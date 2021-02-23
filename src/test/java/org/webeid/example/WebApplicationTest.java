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

package org.webeid.example;

import mockit.Mock;
import mockit.MockUp;
import org.digidoc4j.impl.asic.AsicSignatureFinalizer;
import org.digidoc4j.impl.asic.xades.XadesSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.webeid.example.service.dto.DigestDTO;
import org.webeid.example.testutil.Dates;
import org.webeid.example.testutil.HttpHelper;
import org.webeid.example.testutil.ObjectMother;
import org.webeid.security.validator.AuthTokenValidatorData;
import org.webeid.security.validator.validators.SubjectCertificateNotRevokedValidator;

import javax.cache.Cache;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
@WebAppConfiguration
public class WebApplicationTest {

    @Autowired
    Cache<String, LocalDateTime> cache;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private javax.servlet.Filter[] springSecurityFilterChain;

    private static final String TEST_NONCE = "a/wAFpMaXojLIaEmeOvviRNoBVqHBM/Mm9JV28ZcaIo=";
    private static DefaultMockMvcBuilder mvcBuilder;

    @BeforeEach
    public void setup() {
        mvcBuilder = MockMvcBuilders.webAppContextSetup(context).addFilters(springSecurityFilterChain);
    }

    @Test
    public void testRoot() throws Exception {
        // @formatter:off
        MockHttpServletResponse response = mvcBuilder
            .build()
            .perform(get("/"))
            .andReturn()
            .getResponse();
        // @formatter:on
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        System.out.println(response.getContentAsString());
    }

    @Test
    public void testHappyFlow_LoginUploadPrepareSignDownload() throws Exception {

        // Arrange
        new MockUp<SubjectCertificateNotRevokedValidator>() {
            @Mock
            public void validateCertificateNotRevoked(AuthTokenValidatorData actualTokenData) {
                // Do not call real OCSP service in tests.
            }
        };

        new MockUp<AsicSignatureFinalizer>() {
            @Mock
            public void validateOcspResponse(XadesSignature xadesSignature) {
                // Do not call real OCSP service in tests.
            }
        };

        cache.put(TEST_NONCE, LocalDateTime.now().plusMinutes(5));

        final MockHttpSession session = new MockHttpSession();

        Dates.setMockedDate(Dates.create("2020-04-14T13:36:49Z"));

        // Act and assert
        mvcBuilder.build().perform(get("/auth/challenge"));

        MockHttpServletResponse response = HttpHelper.login(mvcBuilder, session, ObjectMother.mockAuthToken());
        assertEquals("{\"sub\":\"JAAK-KRISTJAN JÕEORG, PNOEE-38001085718\",\"auth\":[\"ROLE_USER\"]}", response.getContentAsString());

        /* Example how to test file upload.
        response = HttpHelper.upload(mvcBuilder, session, ObjectMother.mockMultipartFile());
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        */

        response = HttpHelper.prepare(mvcBuilder, session, ObjectMother.mockPrepareRequest());
        assertEquals(HttpStatus.OK.value(), response.getStatus());

        DigestDTO digestDTO = ObjectMother.jsonStringToBean(response.getContentAsString(), DigestDTO.class);

        response = HttpHelper.sign(mvcBuilder, session, ObjectMother.mockSignRequest(digestDTO.getHash()));
        assertEquals(HttpStatus.OK.value(), response.getStatus());

        response = HttpHelper.download(mvcBuilder, session, ObjectMother.mockSignRequest(digestDTO.getHash()));
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("attachment; filename=example-for-signing.asice", response.getHeader("Content-Disposition"));
    }
}
