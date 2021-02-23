/*
 * Copyright (c) 2020 The Web eID Project
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

package org.webeid.example.service;

import com.google.common.io.ByteStreams;
import org.apache.commons.io.FilenameUtils;
import org.digidoc4j.*;
import org.digidoc4j.utils.TokenAlgorithmSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.webeid.example.config.YAMLConfig;
import org.webeid.example.service.dto.CertificateDTO;
import org.webeid.example.service.dto.DigestDTO;
import org.webeid.example.service.dto.FileDTO;
import org.webeid.example.service.dto.SignatureDTO;
import org.webeid.example.web.rest.SigningController;

import javax.servlet.http.HttpSession;
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Objects;

@Service
public class SigningService {

    private static final String SESSION_ATTR_FILE = "file-to-sign";
    private static final String SESSION_ATTR_CONTAINER = "container-to-sign";
    private static final String SESSION_ATTR_DATA = "data-to-sign";
    private static final Logger LOG = LoggerFactory.getLogger(SigningController.class);
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
     * Creates a {@link Container} using given name and files.
     *
     * @param fileDTO container file
     * @return new {@link Container} instance
     */
    public FileDTO createContainer(FileDTO fileDTO) {

        LOG.info("Creating container for file '{}'", fileDTO.getName());
        ContainerBuilder builder = ContainerBuilder.aContainer(Container.DocumentType.ASICE);

        byte[] fileBytes = Base64.getDecoder().decode(fileDTO.getBase64String().getBytes(StandardCharsets.UTF_8));
        DataFile dataFile = new DataFile(fileBytes, fileDTO.getName(), fileDTO.getContentType());
        builder.withDataFile(dataFile);

        LOG.info("Successfully created container '{}'", fileDTO.getName());
        Container containerToSign = builder.withConfiguration(signingConfiguration).build();

        currentSession().setAttribute(SESSION_ATTR_CONTAINER, containerToSign);
        currentSession().setAttribute(SESSION_ATTR_FILE, fileDTO);

        FileDTO newFileDTO = new FileDTO();
        newFileDTO.setName(fileDTO.getName());

        return newFileDTO;
    }

    /**
     * Prepares given container {@link Container} for the signature process.
     *
     * @param certificateDTO user's X.509 certificate
     * @return data to be signed
     */
    public DigestDTO prepareContainer(CertificateDTO certificateDTO) throws CertificateException, NoSuchAlgorithmException {
        X509Certificate certificate = certificateDTO.toX509Certificate();
        FileDTO fileDTO = (FileDTO) Objects.requireNonNull(currentSession().getAttribute(SESSION_ATTR_FILE));
        Container containerToPrepare = (Container) Objects.requireNonNull(currentSession().getAttribute(SESSION_ATTR_CONTAINER));

        String containerName = generateContainerName(fileDTO.getName());
        LOG.info("Preparing container for signing for file '{}'", containerName);

        DataToSign dataToSign = SignatureBuilder
                .aSignature(containerToPrepare)
                .withSignatureProfile(SignatureProfile.LT) // AIA OCSP is supported for signatures with LT or LTA profile.
                .withSigningCertificate(certificate)
                .withSignatureDigestAlgorithm(TokenAlgorithmSupport.determineSignatureDigestAlgorithm(certificate))
                .buildDataToSign();

        currentSession().setAttribute(SESSION_ATTR_DATA, dataToSign);

        LOG.info("Successfully prepared container for signing for file '{}'", containerName);

        final String digestAlgorithm = certificateDTO.getSupportedAlgorithmNames().contains("SHA384") ?
                "SHA-384" : "SHA-256";

        final byte[] digest = MessageDigest.getInstance(digestAlgorithm).digest(dataToSign.getDataToSign());

        DigestDTO digestDTO = new DigestDTO();
        digestDTO.setHash(DatatypeConverter.printBase64Binary(digest));
        digestDTO.setAlgorithm(digestAlgorithm);

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
        try {
            FileDTO fileDTO = (FileDTO) Objects.requireNonNull(currentSession().getAttribute(SESSION_ATTR_FILE));
            Container containerToSign = (Container) Objects.requireNonNull(currentSession().getAttribute(SESSION_ATTR_CONTAINER));
            DataToSign dataToSign = (DataToSign) Objects.requireNonNull(currentSession().getAttribute(SESSION_ATTR_DATA));

            byte[] signatureBytes = DatatypeConverter.parseBase64Binary(signatureDTO.getBase64Signature());
            Signature signature = dataToSign.finalize(signatureBytes);
            containerToSign.addSignature(signature);
            currentSession().setAttribute(SESSION_ATTR_CONTAINER, containerToSign);

            FileDTO result = new FileDTO();
            result.setName(generateContainerName(fileDTO.getName()));
            return result;
        } catch (Exception ex) {
            LOG.error("Signing of container caused an error", ex);
            throw new RuntimeException("Signing of container caused an error");
        }
    }

    private String generateContainerName(String fileName) {
        return FilenameUtils.removeExtension(fileName) + ".asice";
    }


    public String getContainerName() {
        FileDTO fileDTO = (FileDTO) Objects.requireNonNull(currentSession().getAttribute(SESSION_ATTR_FILE));
        return generateContainerName(fileDTO.getName());
    }

    public ByteArrayResource getSignedContainerAsResource() throws IOException {
        Container signedContainer = (Container) Objects.requireNonNull(currentSession().getAttribute(SESSION_ATTR_CONTAINER));
        return new ByteArrayResource(ByteStreams.toByteArray(signedContainer.saveAsStream()));
    }
}
