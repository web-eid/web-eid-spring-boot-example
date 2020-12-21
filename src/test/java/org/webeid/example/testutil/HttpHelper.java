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

import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder;
import org.webeid.example.security.dto.AuthTokenDTO;
import org.webeid.example.service.dto.CertificateDTO;
import org.webeid.example.service.dto.SignatureDTO;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class HttpHelper {

    public static MockHttpServletResponse login(DefaultMockMvcBuilder mvcBuilder, MockHttpSession session, AuthTokenDTO authTokenDTO) throws Exception {
        // @formatter:off
        return mvcBuilder
                .build()
                .perform(post("/auth/login")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ObjectMother.toJson(authTokenDTO)))
                .andReturn()
                .getResponse();
        // @formatter:on
    }

    public static MockHttpServletResponse upload(DefaultMockMvcBuilder mvcBuilder, MockHttpSession session, MockMultipartFile mockMultipartFile) throws Exception {
        // @formatter:off
        return mvcBuilder
                .build()
                .perform(MockMvcRequestBuilders.multipart("/sign/upload")
                        .file(mockMultipartFile)
                        .session(session))
                .andReturn()
                .getResponse();
        // @formatter:on
    }

    public static MockHttpServletResponse prepare(DefaultMockMvcBuilder mvcBuilder, MockHttpSession session, CertificateDTO certificateDTO) throws Exception {
        // @formatter:off
        return mvcBuilder
                .build()
                .perform(post("/sign/prepare")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ObjectMother.toJson(certificateDTO)))
                .andReturn()
                .getResponse();
        // @formatter:on
    }

    public static MockHttpServletResponse sign(DefaultMockMvcBuilder mvcBuilder, MockHttpSession session, SignatureDTO signatureDTO) throws Exception {
        // @formatter:off
        return mvcBuilder
                .build()
                .perform(post("/sign/sign")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ObjectMother.toJson(signatureDTO)))
                .andReturn()
                .getResponse();
        // @formatter:on
    }

    public static MockHttpServletResponse download(DefaultMockMvcBuilder mvcBuilder, MockHttpSession session, SignatureDTO mockSignRequest) throws Exception {
        // @formatter:off
        return mvcBuilder
                .build()
                .perform(get("/sign/download")
                        .session(session))
                .andReturn()
                .getResponse();
        // @formatter:on
    }
}
