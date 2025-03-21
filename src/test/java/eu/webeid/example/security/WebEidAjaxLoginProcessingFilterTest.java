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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;

import java.io.BufferedReader;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WebEidAjaxLoginProcessingFilterTest {

    private static final String AUTH_TOKEN = "{\"auth-token\":" +
            "{\"algorithm\":\"ES384\"," +
            "\"certificate\":\"MIIEAzCCA2WgAwIBAgIQHWbVWxCkcYxbzz9nBzGrDzAKBggqhkjOPQQDBDBgMQswCQYDVQQGEwJFRTEbMBkGA1UECgwSU0sgSUQgU29sdXRpb25zIEFTMRcwFQYDVQRhDA5OVFJFRS0xMDc0NzAxMzEbMBkGA1UEAwwSVEVTVCBvZiBFU1RFSUQyMDE4MB4XDTE4MTAyMzE1MzM1OVoXDTIzMTAyMjIxNTk1OVowfzELMAkGA1UEBhMCRUUxKjAoBgNVBAMMIUrDlUVPUkcsSkFBSy1LUklTVEpBTiwzODAwMTA4NTcxODEQMA4GA1UEBAwHSsOVRU9SRzEWMBQGA1UEKgwNSkFBSy1LUklTVEpBTjEaMBgGA1UEBRMRUE5PRUUtMzgwMDEwODU3MTgwdjAQBgcqhkjOPQIBBgUrgQQAIgNiAAQ/u+9IncarVpgrACN6aRgUiT9lWC9H7llnxoEXe8xoCI982Md8YuJsVfRdeG5jwVfXe0N6KkHLFRARspst8qnACULkqFNat/Kj+XRwJ2UANeJ3Gl5XBr+tnLNuDf/UiR6jggHDMIIBvzAJBgNVHRMEAjAAMA4GA1UdDwEB/wQEAwIDiDBHBgNVHSAEQDA+MDIGCysGAQQBg5EhAQIBMCMwIQYIKwYBBQUHAgEWFWh0dHBzOi8vd3d3LnNrLmVlL0NQUzAIBgYEAI96AQIwHwYDVR0RBBgwFoEUMzgwMDEwODU3MThAZWVzdGkuZWUwHQYDVR0OBBYEFOTddHnA9rJtbLwhBNyn0xZTQGCMMGEGCCsGAQUFBwEDBFUwUzBRBgYEAI5GAQUwRzBFFj9odHRwczovL3NrLmVlL2VuL3JlcG9zaXRvcnkvY29uZGl0aW9ucy1mb3ItdXNlLW9mLWNlcnRpZmljYXRlcy8TAkVOMCAGA1UdJQEB/wQWMBQGCCsGAQUFBwMCBggrBgEFBQcDBDAfBgNVHSMEGDAWgBTAhJkpxE6fOwI09pnhClYACCk+ezBzBggrBgEFBQcBAQRnMGUwLAYIKwYBBQUHMAGGIGh0dHA6Ly9haWEuZGVtby5zay5lZS9lc3RlaWQyMDE4MDUGCCsGAQUFBzAChilodHRwOi8vYy5zay5lZS9UZXN0X29mX0VTVEVJRDIwMTguZGVyLmNydDAKBggqhkjOPQQDBAOBiwAwgYcCQgHYElkX4vn821JR41akI/lpexCnJFUf4GiOMbTfzAxpZma333R8LNrmI4zbzDp03hvMTzH49g1jcbGnaCcbboS8DAJBObenUp++L5VqldHwKAps61nM4V+TiLqD0jILnTzl+pV+LexNL3uGzUfvvDNLHnF9t6ygi8+Bsjsu3iHHyM1haKM=\"," +
            "\"issuerApp\":\"https://web-eid.eu/web-eid-app/releases/2.0.0+976\"," +
            "\"signature\":\"Z+r6IIx0lmXNTHrlJOTknVaOYszXba5ko1e8rjWJXd4nzIVsxeTps/Revg+tuKREkkIuIKhwvOnARdWV6vBmhjlpRUZn9aNPSDRP98T/0sPZgDp31hwsWfCAYnPzzYC9\"," +
            "\"version\":\"web-eid:1\"}}";

    @Test
    void testAttemptAuthentication() throws Exception {
        final HttpServletRequest request = mock(HttpServletRequest.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);
        when(request.getMethod()).thenReturn(HttpMethod.POST.name());
        when(request.getHeader("Content-type")).thenReturn("application/json");
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(AUTH_TOKEN)));

        final AuthenticationManager authenticationManager = mock(AuthenticationManager.class);

        assertDoesNotThrow(() ->
                new WebEidAjaxLoginProcessingFilter("/auth/login", authenticationManager)
                        .attemptAuthentication(request, response));
    }
}