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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Component;
import org.webeid.example.security.dto.AuthTokenDTO;
import org.webeid.security.exceptions.TokenValidationException;
import org.webeid.security.util.CertUtil;
import org.webeid.security.validator.AuthTokenValidator;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

/**
 * Parses JWT from token string inside AuthTokenDTO and attempts authentication.
 */
@Component
public class AuthTokenDTOAuthenticationProvider implements AuthenticationProvider {
    public static final String ROLE_USER = "ROLE_USER";
    private static final GrantedAuthority USER_ROLE = new SimpleGrantedAuthority(ROLE_USER);

    private static final Logger LOG = LoggerFactory.getLogger(AuthTokenDTOAuthenticationProvider.class);

    @Autowired
    private AuthTokenValidator tokenValidator;

    @Override
    public Authentication authenticate(Authentication auth) throws AuthenticationException {
        LOG.info("authenticate(): {}", auth);

        final PreAuthenticatedAuthenticationToken authentication = (PreAuthenticatedAuthenticationToken) auth;

        final String token = ((AuthTokenDTO) authentication.getCredentials()).getToken();

        final List<GrantedAuthority> authorities = new ArrayList<>(2);
        authorities.add(USER_ROLE);

        try {
            X509Certificate userCertificate = tokenValidator.validate(token);
            return new PreAuthenticatedAuthenticationToken(getPrincipalFromCertificate(userCertificate), null, authorities);
        } catch (TokenValidationException e) {
            LOG.warn("Token validation has failed", e);
            throw new AuthenticationServiceException("Token validation failed: " + e.getMessage());
        } catch (CertificateEncodingException e) {
            LOG.warn("Failed to extract subject fields from certificate", e);
            throw new AuthenticationServiceException("Incorrect certificate subject fields: " + e.getMessage());
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        LOG.info("supports(): {}", authentication);
        return PreAuthenticatedAuthenticationToken.class.equals(authentication);
    }

    // FIXME: create a proper principal object
    private String getPrincipalFromCertificate(X509Certificate userCertificate) throws CertificateEncodingException {
        return CertUtil.getSubjectGivenName(userCertificate) + ' ' +
                CertUtil.getSubjectSurname(userCertificate) + ", " +
                CertUtil.getSubjectIdCode(userCertificate);
    }
}
