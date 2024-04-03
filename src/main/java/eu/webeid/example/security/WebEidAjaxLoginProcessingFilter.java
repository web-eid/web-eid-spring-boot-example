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

package eu.webeid.example.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import eu.webeid.example.security.ajax.AjaxAuthenticationFailureHandler;
import eu.webeid.example.security.ajax.AjaxAuthenticationSuccessHandler;
import eu.webeid.example.security.dto.AuthTokenDTO;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.security.web.authentication.session.SessionFixationProtectionStrategy;
import org.springframework.security.web.context.SecurityContextRepository;

public class WebEidAjaxLoginProcessingFilter extends AbstractAuthenticationProcessingFilter {
    private static final Logger LOG = LoggerFactory.getLogger(WebEidAjaxLoginProcessingFilter.class);
    private final SecurityContextRepository securityContextRepository;

    public WebEidAjaxLoginProcessingFilter(
        String defaultFilterProcessesUrl,
        AuthenticationManager authenticationManager,
        SecurityContextRepository securityContextRepository
    ) {
        super(defaultFilterProcessesUrl);
        this.setAuthenticationManager(authenticationManager);
        this.setAuthenticationSuccessHandler(new AjaxAuthenticationSuccessHandler());
        this.setAuthenticationFailureHandler(new AjaxAuthenticationFailureHandler());
        setSessionAuthenticationStrategy(new SessionFixationProtectionStrategy());
        this.securityContextRepository = securityContextRepository;
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

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        super.successfulAuthentication(request, response, chain, authResult); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/OverriddenMethodBody
        securityContextRepository.saveContext(SecurityContextHolder.getContext(), request, response);
    }
}
