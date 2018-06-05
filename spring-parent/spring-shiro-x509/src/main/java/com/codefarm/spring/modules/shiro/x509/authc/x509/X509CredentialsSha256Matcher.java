package com.codefarm.spring.modules.shiro.x509.authc.x509;

import java.security.cert.CertificateEncodingException;

import org.apache.shiro.crypto.hash.Sha256Hash;

public class X509CredentialsSha256Matcher extends
        AbstractX509CredentialsMatcher
{
    
    @Override
    public boolean doX509CredentialsMatch(X509AuthenticationToken token,
            X509AuthenticationInfo info)
    {
        try
        {
            String clientCertSha256 = new Sha256Hash(token.getX509Certificate()
                    .getEncoded()).toHex();
            String subjectCertSha256 = new Sha256Hash(info.getX509Certificate()
                    .getEncoded()).toHex();
            
            boolean match = clientCertSha256.equals(subjectCertSha256);
            
            if (match)
            {
                LOGGER.trace("Client certificate Sha256 hash match the one provided by the Realm, will return true");
            }
            else
            {
                LOGGER.trace("Client certificate Sha256 hash ({}) do not match the one provided by the Realm ({}), will return false",
                        clientCertSha256,
                        subjectCertSha256);
            }
            
            return match;
            
        }
        catch (CertificateEncodingException ex)
        {
            LOGGER.trace("Unable to do credentials matching", ex);
            return false;
        }
        
    }
    
}
