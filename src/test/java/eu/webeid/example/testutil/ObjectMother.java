/*
 * Copyright (c) 2020-2023 Estonian Information System Authority
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

package eu.webeid.example.testutil;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.webeid.security.authtoken.WebEidAuthToken;
import org.apache.commons.lang3.ArrayUtils;
import org.digidoc4j.DigestAlgorithm;
import org.digidoc4j.exceptions.DigiDoc4JException;
import eu.webeid.example.security.dto.AuthTokenDTO;
import eu.webeid.example.service.dto.CertificateDTO;
import eu.webeid.example.service.dto.SignatureDTO;

import javax.xml.bind.DatatypeConverter;
import java.io.FileInputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Base64;

public class ObjectMother {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String TEST_PKI_CONTAINER = "src/test/resources/signout.p12";
    private static final String TEST_PKI_CONTAINER_PASSWORD = "test";
    private static final WebEidAuthToken VALID_AUTH_TOKEN;

    static {
        try {
            VALID_AUTH_TOKEN = MAPPER.readValue(
                    "{\"algorithm\":\"ES384\"," +
                            "\"unverifiedCertificate\":\"MIIEAzCCA2WgAwIBAgIQHWbVWxCkcYxbzz9nBzGrDzAKBggqhkjOPQQDBDBgMQswCQYDVQQGEwJFRTEbMBkGA1UECgwSU0sgSUQgU29sdXRpb25zIEFTMRcwFQYDVQRhDA5OVFJFRS0xMDc0NzAxMzEbMBkGA1UEAwwSVEVTVCBvZiBFU1RFSUQyMDE4MB4XDTE4MTAyMzE1MzM1OVoXDTIzMTAyMjIxNTk1OVowfzELMAkGA1UEBhMCRUUxKjAoBgNVBAMMIUrDlUVPUkcsSkFBSy1LUklTVEpBTiwzODAwMTA4NTcxODEQMA4GA1UEBAwHSsOVRU9SRzEWMBQGA1UEKgwNSkFBSy1LUklTVEpBTjEaMBgGA1UEBRMRUE5PRUUtMzgwMDEwODU3MTgwdjAQBgcqhkjOPQIBBgUrgQQAIgNiAAQ/u+9IncarVpgrACN6aRgUiT9lWC9H7llnxoEXe8xoCI982Md8YuJsVfRdeG5jwVfXe0N6KkHLFRARspst8qnACULkqFNat/Kj+XRwJ2UANeJ3Gl5XBr+tnLNuDf/UiR6jggHDMIIBvzAJBgNVHRMEAjAAMA4GA1UdDwEB/wQEAwIDiDBHBgNVHSAEQDA+MDIGCysGAQQBg5EhAQIBMCMwIQYIKwYBBQUHAgEWFWh0dHBzOi8vd3d3LnNrLmVlL0NQUzAIBgYEAI96AQIwHwYDVR0RBBgwFoEUMzgwMDEwODU3MThAZWVzdGkuZWUwHQYDVR0OBBYEFOTddHnA9rJtbLwhBNyn0xZTQGCMMGEGCCsGAQUFBwEDBFUwUzBRBgYEAI5GAQUwRzBFFj9odHRwczovL3NrLmVlL2VuL3JlcG9zaXRvcnkvY29uZGl0aW9ucy1mb3ItdXNlLW9mLWNlcnRpZmljYXRlcy8TAkVOMCAGA1UdJQEB/wQWMBQGCCsGAQUFBwMCBggrBgEFBQcDBDAfBgNVHSMEGDAWgBTAhJkpxE6fOwI09pnhClYACCk+ezBzBggrBgEFBQcBAQRnMGUwLAYIKwYBBQUHMAGGIGh0dHA6Ly9haWEuZGVtby5zay5lZS9lc3RlaWQyMDE4MDUGCCsGAQUFBzAChilodHRwOi8vYy5zay5lZS9UZXN0X29mX0VTVEVJRDIwMTguZGVyLmNydDAKBggqhkjOPQQDBAOBiwAwgYcCQgHYElkX4vn821JR41akI/lpexCnJFUf4GiOMbTfzAxpZma333R8LNrmI4zbzDp03hvMTzH49g1jcbGnaCcbboS8DAJBObenUp++L5VqldHwKAps61nM4V+TiLqD0jILnTzl+pV+LexNL3uGzUfvvDNLHnF9t6ygi8+Bsjsu3iHHyM1haKM=\"," +
                            "\"appVersion\":\"https://web-eid.eu/web-eid-app/releases/2.0.0+0\"," +
                            "\"signature\":\"tbMTrZD4CKUj6atjNCHZruIeyPFAEJk2htziQ1t08BSTyA5wKKqmNmzsJ7562hWQ6+tJd6nlidHGE5jVVJRKmPtNv3f9gbT2b7RXcD4t5Pjn8eUCBCA4IX99Af32Z5ln\"," +
                            "\"format\":\"web-eid:1\"}",
                    WebEidAuthToken.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Token parsing failed");
        }
    }

    public static final String VALID_CHALLENGE_NONCE = "12345678123456781234567812345678912356789123";

    public static AuthTokenDTO mockAuthToken() {
        AuthTokenDTO authToken = new AuthTokenDTO();
        authToken.setToken(VALID_AUTH_TOKEN);
        return authToken;
    }

    public static String toJson(Object object) throws JsonProcessingException {
        return MAPPER.writeValueAsString(object);
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
        return MAPPER.readValue(jsonString, valueType);
    }

    public static CertificateDTO mockPrepareRequest() {
        CertificateDTO certificateDTO = new CertificateDTO();
        certificateDTO.setCertificate(mockCertificateInBase64());
        return certificateDTO;
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
