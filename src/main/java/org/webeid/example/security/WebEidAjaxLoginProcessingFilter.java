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

package org.webeid.example.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.webeid.example.security.ajax.AjaxAuthenticationFailureHandler;
import org.webeid.example.security.ajax.AjaxAuthenticationSuccessHandler;
import org.webeid.example.security.dto.AuthTokenDTO;

public class WebEidAjaxLoginProcessingFilter extends AbstractAuthenticationProcessingFilter {
    private static final Logger LOG = LoggerFactory.getLogger(WebEidAjaxLoginProcessingFilter.class);

    public WebEidAjaxLoginProcessingFilter(
        String defaultFilterProcessesUrl,
        AuthenticationManager authenticationManager
    ) {
        super(defaultFilterProcessesUrl);
        this.setAuthenticationManager(authenticationManager);
        this.setAuthenticationSuccessHandler(new AjaxAuthenticationSuccessHandler());
        this.setAuthenticationFailureHandler(new AjaxAuthenticationFailureHandler());
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
        throws AuthenticationException, IOException {
        if (!HttpMethod.POST.name().equals(request.getMethod())) {
            LOG.warn("HttpMethod not supported: {}", request.getMethod());
            throw new AuthenticationServiceException("HttpMethod not supported: " + request.getMethod());
        }
        final String contentType = request.getHeader("Content-type");
        if (contentType == null || !contentType.startsWith("application/json")) {
            LOG.warn("Content type not supported: {}", contentType);
            throw new AuthenticationServiceException("Content type not supported: " + contentType);
        }

        LOG.info("attemptAuthentication(): Reading request body");
        final ObjectMapper objectMapper = new ObjectMapper();
        final AuthTokenDTO authTokenDTO = objectMapper.readValue(request.getReader(), AuthTokenDTO.class);
        LOG.info("attemptAuthentication(): Creating token");
        final PreAuthenticatedAuthenticationToken token = new PreAuthenticatedAuthenticationToken(null, authTokenDTO);
        LOG.info("attemptAuthentication(): Calling authentication manager");
        return getAuthenticationManager().authenticate(token);
    }
}
