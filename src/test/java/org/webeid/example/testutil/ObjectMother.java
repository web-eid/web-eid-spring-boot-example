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

package org.webeid.example.testutil;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.ArrayUtils;
import org.digidoc4j.DigestAlgorithm;
import org.digidoc4j.exceptions.DigiDoc4JException;
import org.springframework.mock.web.MockMultipartFile;
import org.webeid.example.security.dto.AuthTokenDTO;
import org.webeid.example.service.dto.CertificateDTO;
import org.webeid.example.service.dto.SignatureDTO;

import javax.xml.bind.DatatypeConverter;
import java.io.FileInputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Base64;

public class ObjectMother {

    public static final String TEST_PKI_CONTAINER = "src/test/resources/signout.p12";
    public static final String TEST_PKI_CONTAINER_PASSWORD = "test";
    private static final ObjectMapper mapper = new ObjectMapper();

    public static AuthTokenDTO mockAuthToken() throws JsonProcessingException {
        AuthTokenDTO authToken = new AuthTokenDTO();
        authToken.setToken(Tokens.SIGNED);
        return authToken;
    }

    public static String toJson(Object object) throws JsonProcessingException {
        return mapper.writeValueAsString(object);
    }


    public static MockMultipartFile mockMultipartFile() {
        return new MockMultipartFile("file", "test-file.txt", "text/plain", "some xml".getBytes());
    }

    public static String mockCertificateInBase64() {
        try {
            X509Certificate certificate = getSigningCert();
            byte[] derEncodedCertificate = certificate.getEncoded();
            return DatatypeConverter.printBase64Binary(derEncodedCertificate);
        } catch (Exception e) {
            throw new RuntimeException("Certificate loading failed", e);
        }
    }

    public static String mockSignatureInBase64(String digestToSign) {
        return signDigest(DatatypeConverter.parseBase64Binary(digestToSign));
    }

    public static <T> T jsonStringToBean(String jsonString, Class<T> valueType) throws JsonProcessingException {
        return mapper.readValue(jsonString, valueType);
    }

    public static CertificateDTO mockPrepareRequest() {
        CertificateDTO certificateDTODTO = new CertificateDTO();
        certificateDTODTO.setBase64String(mockCertificateInBase64());
        return certificateDTODTO;
    }

    public static SignatureDTO mockSignRequest(String digestToSign) {
        SignatureDTO signatureDTO = new SignatureDTO();
        signatureDTO.setBase64Signature(mockSignatureInBase64(digestToSign));
        return signatureDTO;
    }

    private static X509Certificate getSigningCert() {
        try {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            try (FileInputStream stream = new FileInputStream(TEST_PKI_CONTAINER)) {
                keyStore.load(stream, TEST_PKI_CONTAINER_PASSWORD.toCharArray());
            }
            return (X509Certificate) keyStore.getCertificate("1");
        } catch (Exception e) {
            throw new RuntimeException("Loading signer cert failed");
        }
    }

    private static byte[] sign(byte[] dataToSign) {
        try {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            try (FileInputStream stream = new FileInputStream(TEST_PKI_CONTAINER)) {
                keyStore.load(stream, TEST_PKI_CONTAINER_PASSWORD.toCharArray());
            }
            PrivateKey privateKey = (PrivateKey) keyStore.getKey("1", TEST_PKI_CONTAINER_PASSWORD.toCharArray());
            final String javaSignatureAlgorithm = "NONEwith" + privateKey.getAlgorithm();

            return encrypt(javaSignatureAlgorithm, privateKey, addOID(dataToSign));
        } catch (Exception e) {
            throw new DigiDoc4JException("Loading private key failed");
        }
    }

    private static byte[] addOID(byte[] digest) {
        return ArrayUtils.addAll(DigestAlgorithm.SHA256.digestInfoPrefix(), digest);
    }

    private static byte[] encrypt(final String javaSignatureAlgorithm, final PrivateKey privateKey, final byte[] bytes) {
        try {
            java.security.Signature signature = java.security.Signature.getInstance(javaSignatureAlgorithm);
            signature.initSign(privateKey);
            signature.update(bytes);
            return signature.sign();
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    private static String signDigest(byte[] digestToSign) {
        byte[] signatureValue = sign(digestToSign);
        return Base64.getEncoder().encodeToString(signatureValue);
    }

}
