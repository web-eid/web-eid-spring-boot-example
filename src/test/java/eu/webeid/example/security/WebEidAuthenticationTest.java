/*
 * Copyright (c) 2020-2025 Estonian Information System Authority
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

package eu.webeid.example.security;

import eu.webeid.security.certificate.CertificateLoader;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import java.security.cert.X509Certificate;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class WebEidAuthenticationTest {

    private static final String ORGANIZATION_CERT = "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";

    @Test
    void whenOrganizationCertificate_thenSucceeds() throws Exception {
        final X509Certificate certificate = CertificateLoader.decodeCertificateFromBase64(ORGANIZATION_CERT);
        final Authentication authentication = WebEidAuthentication.fromCertificate(certificate, Collections.emptyList());
        assertThat(authentication.getPrincipal()).isEqualTo("Testijad.ee isikutuvastus");
    }

}