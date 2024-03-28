/*
 * Copyright (c) 2020-2024 Estonian Information System Authority
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package eu.webeid.example.service;

import eu.webeid.example.config.YAMLConfig;
import eu.webeid.example.security.WebEidAuthentication;
import eu.webeid.example.service.dto.CertificateDTO;
import eu.webeid.example.service.dto.DigestDTO;
import eu.webeid.example.service.dto.FileDTO;
import eu.webeid.example.service.dto.SignatureDTO;
import eu.webeid.security.certificate.CertificateData;
import org.apache.commons.io.FilenameUtils;
import org.digidoc4j.Configuration;
import org.digidoc4j.Container;
import org.digidoc4j.ContainerBuilder;
import org.digidoc4j.DataFile;
import org.digidoc4j.DataToSign;
import org.digidoc4j.DigestAlgorithm;
import org.digidoc4j.Signature;
import org.digidoc4j.SignatureBuilder;
import org.digidoc4j.SignatureProfile;
import org.digidoc4j.utils.TokenAlgorithmSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpSession;
import jakarta.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Objects;

@Service
public class SigningService {

    private static final String SESSION_ATTR_FILE = "file-to-sign";
    private static final String SESSION_ATTR_CONTAINER = "container-to-sign";
    private static final String SESSION_ATTR_DATA = "data-to-sign";
    private static final Logger LOG = LoggerFactory.getLogger(SigningService.class);
    private final Configuration signingConfiguration;

    ObjectFactory<HttpSession> httpSessionFactory;

    public SigningService(ObjectFactory<HttpSession> httpSessionFactory, YAMLConfig yamlConfig) {
        this.httpSessionFactory = httpSessionFactory;
        signingConfiguration = Configuration.of(yamlConfig.getUseDigiDoc4jProdConfiguration() ?
                Configuration.Mode.PROD : Configuration.Mode.TEST);
        // Use automatic AIA OCSP URL selection from certificate for signatures.
        signingConfiguration.setPreferAiaOcsp(true);
    }

    private HttpSession currentSession() {
        return httpSessionFactory.getObject();
    }

    /**
     * Prepares given container {@link Container} for the signature process.
     *
     * @param certificateDTO user's X.509 certificate
     * @param authentication authenticated principal
     * @return data to be signed
     */
    public DigestDTO prepareContainer(CertificateDTO certificateDTO, WebEidAuthentication authentication) throws CertificateException, NoSuchAlgorithmException, IOException {
        X509Certificate certificate = certificateDTO.toX509Certificate();
        if (!authentication.getIdCode().equals(CertificateData.getSubjectIdCode(certificate))) {
            throw new IllegalArgumentException("Authenticated subject ID code differs from " +
                    "signing certificate subject ID code");
        }

        FileDTO fileDTO = FileDTO.getExampleForSigningFromResources();
        Container containerToSign = getContainerToSign(fileDTO);
        String containerName = generateContainerName(fileDTO.getName());

        currentSession().setAttribute(SESSION_ATTR_CONTAINER, containerToSign);
        currentSession().setAttribute(SESSION_ATTR_FILE, fileDTO);

        LOG.info("Preparing container for signing for file '{}'", containerName);

        final DigestAlgorithm signatureDigestAlgorithm = TokenAlgorithmSupport.determineSignatureDigestAlgorithm(certificate);
        final String digestAlgorithmName = signatureDigestAlgorithm.uri().getRef()
                .toUpperCase().replace("SHA", "SHA-"); // SHA256 -> SHA-256
        if (!certificateDTO.getSupportedHashFunctionNames().contains(digestAlgorithmName)) {
            throw new IllegalArgumentException("Determined signature digest algorithm '" + digestAlgorithmName +
                    "' is not supported. Supported algorithms are: " + String.join(", ", certificateDTO.getSupportedHashFunctionNames()));
        }

        DataToSign dataToSign = SignatureBuilder
                .aSignature(containerToSign)
                .withSignatureProfile(SignatureProfile.LT) // AIA OCSP is supported for signatures with LT or LTA profile.
                .withSigningCertificate(certificate)
                .withSignatureDigestAlgorithm(signatureDigestAlgorithm)
                .buildDataToSign();

        currentSession().setAttribute(SESSION_ATTR_DATA, dataToSign);

        LOG.info("Successfully prepared container for signing for file '{}'", containerName);

        final byte[] digest = signatureDigestAlgorithm.getDssDigestAlgorithm().getMessageDigest()
                .digest(dataToSign.getDataToSign());

        DigestDTO digestDTO = new DigestDTO();
        digestDTO.setHash(DatatypeConverter.printBase64Binary(digest));
        digestDTO.setHashFunction(digestAlgorithmName);

        return digestDTO;
    }

    /**
     * Signs a {@link Container} using given {@link SignatureDTO}.
     * Container to sign is taken from the current session.
     *
     * @param signatureDTO signature DTO
     * @return fileDTO
     */
    public FileDTO signContainer(SignatureDTO signatureDTO) {
        FileDTO fileDTO = (FileDTO) Objects.requireNonNull(currentSession().getAttribute(SESSION_ATTR_FILE));
        Container containerToSign = (Container) Objects.requireNonNull(currentSession().getAttribute(SESSION_ATTR_CONTAINER));
        DataToSign dataToSign = (DataToSign) Objects.requireNonNull(currentSession().getAttribute(SESSION_ATTR_DATA));

        byte[] signatureBytes = DatatypeConverter.parseBase64Binary(signatureDTO.getBase64Signature());
        Signature signature = dataToSign.finalize(signatureBytes);
        containerToSign.addSignature(signature);
        currentSession().setAttribute(SESSION_ATTR_CONTAINER, containerToSign);

        return new FileDTO(generateContainerName(fileDTO.getName()));
    }

    public String getContainerName() {
        FileDTO fileDTO = (FileDTO) Objects.requireNonNull(currentSession().getAttribute(SESSION_ATTR_FILE));
        return generateContainerName(fileDTO.getName());
    }

    public ByteArrayResource getSignedContainerAsResource() throws IOException {
        Container signedContainer = (Container) Objects.requireNonNull(currentSession().getAttribute(SESSION_ATTR_CONTAINER));
        try (final InputStream stream = signedContainer.saveAsStream()) {
            return new ByteArrayResource(stream.readAllBytes());
        }
    }

    private Container getContainerToSign(FileDTO fileDTO) {
        LOG.info("Creating container for file '{}'", fileDTO.getName());

        final DataFile dataFile = new DataFile(fileDTO.getContentBytes(), fileDTO.getName(), fileDTO.getContentType());
        return ContainerBuilder
                .aContainer(Container.DocumentType.ASICE)
                .withDataFile(dataFile)
                .withConfiguration(signingConfiguration)
                .build();
    }

    private String generateContainerName(String fileName) {
        return FilenameUtils.removeExtension(fileName) + ".asice";
    }

    /*  Here is an example method that demonstrates how to handle file uploads.
     *  See also SigningController.upload().
     *
     * Creates a {@link Container} using given name and files.
     *
     * @param fileDTO container file
     * @return new {@link Container} instance
    public FileDTO createContainer(FileDTO fileDTO) {

        Container containerToSign = getContainerToSign(fileDTO);

        currentSession().setAttribute(SESSION_ATTR_CONTAINER, containerToSign);
        currentSession().setAttribute(SESSION_ATTR_FILE, fileDTO);

        FileDTO newFileDTO = new FileDTO(fileDTO.getName());
        return newFileDTO;
    }
    */
    /* When using uploaded files, retrieve file information and container from the session in prepareContainer().

    FileDTO fileDTO = (FileDTO) Objects.requireNonNull(currentSession().getAttribute(SESSION_ATTR_FILE));
    Container containerToSign = (Container) Objects.requireNonNull(currentSession().getAttribute(SESSION_ATTR_CONTAINER));
     */
}
