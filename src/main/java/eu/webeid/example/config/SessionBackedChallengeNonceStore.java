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

package eu.webeid.example.config;

import org.springframework.beans.factory.ObjectFactory;
import eu.webeid.security.challenge.ChallengeNonce;
import eu.webeid.security.challenge.ChallengeNonceStore;

import jakarta.servlet.http.HttpSession;

public class SessionBackedChallengeNonceStore implements ChallengeNonceStore {

    private static final String CHALLENGE_NONCE_KEY = "challenge-nonce";

    final ObjectFactory<HttpSession> httpSessionFactory;

    public SessionBackedChallengeNonceStore(ObjectFactory<HttpSession> httpSessionFactory) {
        this.httpSessionFactory = httpSessionFactory;
    }

    @Override
    public void put(ChallengeNonce challengeNonce) {
        currentSession().setAttribute(CHALLENGE_NONCE_KEY, challengeNonce);
    }

    @Override
    public ChallengeNonce getAndRemoveImpl() {
        final ChallengeNonce challengeNonce = (ChallengeNonce) currentSession().getAttribute(CHALLENGE_NONCE_KEY);
        currentSession().removeAttribute(CHALLENGE_NONCE_KEY);
        return challengeNonce;
    }

    private HttpSession currentSession() {
        return httpSessionFactory.getObject();
    }

}
