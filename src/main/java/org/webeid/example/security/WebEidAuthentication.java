package org.webeid.example.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.webeid.security.util.CertUtil;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Objects;

public class WebEidAuthentication extends PreAuthenticatedAuthenticationToken implements Authentication {

    private final String idCode;

    public static Authentication fromCertificate(X509Certificate userCertificate, List<GrantedAuthority> authorities) throws CertificateEncodingException {
        final String principalName = getPrincipalNameFromCertificate(userCertificate);
        final String idCode = Objects.requireNonNull(CertUtil.getSubjectIdCode(userCertificate));
        return new WebEidAuthentication(principalName, idCode, authorities);
    }

    public String getIdCode() {
        return idCode;
    }

    private WebEidAuthentication(String principalName, String idCode, List<GrantedAuthority> authorities) {
        super(principalName, idCode, authorities);
        this.idCode = idCode;
    }

    private static String getPrincipalNameFromCertificate(X509Certificate userCertificate) throws CertificateEncodingException {
        return Objects.requireNonNull(CertUtil.getSubjectGivenName(userCertificate)) + ' ' +
                Objects.requireNonNull(CertUtil.getSubjectSurname(userCertificate));
    }

}
