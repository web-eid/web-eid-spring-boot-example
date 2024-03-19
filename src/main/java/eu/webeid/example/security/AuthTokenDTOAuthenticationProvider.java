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

import eu.webeid.example.security.dto.AuthTokenDTO;
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
import eu.webeid.security.authtoken.WebEidAuthToken;
import eu.webeid.security.challenge.ChallengeNonceStore;
import eu.webeid.security.exceptions.AuthTokenException;
import eu.webeid.security.validator.AuthTokenValidator;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
    @Autowired
    private ChallengeNonceStore challengeNonceStore;

    @Override
    public Authentication authenticate(Authentication auth) throws AuthenticationException {
        LOG.info("authenticate(): {}", auth);

        final PreAuthenticatedAuthenticationToken authentication = (PreAuthenticatedAuthenticationToken) auth;
        final WebEidAuthToken authToken = ((AuthTokenDTO) authentication.getCredentials()).getToken();

        final List<GrantedAuthority> authorities = Collections.singletonList(USER_ROLE);

        try {
            final String nonce = challengeNonceStore.getAndRemove().getBase64EncodedNonce();
            final X509Certificate userCertificate = tokenValidator.validate(authToken, nonce);
            return WebEidAuthentication.fromCertificate(userCertificate, authorities);
        } catch (AuthTokenException e) {
            throw new AuthenticationServiceException("Web eID token validation failed", e);
        } catch (CertificateEncodingException e) {
            throw new AuthenticationServiceException("Web eID token has incorrect certificate subject fields", e);
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        LOG.info("supports(): {}", authentication);
        return PreAuthenticatedAuthenticationToken.class.equals(authentication);
    }

}
