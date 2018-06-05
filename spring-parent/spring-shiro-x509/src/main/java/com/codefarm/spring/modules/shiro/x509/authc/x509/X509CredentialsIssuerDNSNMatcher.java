package com.codefarm.spring.modules.shiro.x509.authc.x509;

public class X509CredentialsIssuerDNSNMatcher extends
        AbstractX509CredentialsMatcher
{
    
    @Override
    public boolean doX509CredentialsMatch(X509AuthenticationToken token,
            X509AuthenticationInfo info)
    {
        boolean match = token.getHexSerialNumber()
                .equals(info.getHexSerialNumber())
                && doEquals(token.getIssuerDN(), info.getIssuerDN());
        
        if (match)
        {
            LOGGER.trace("Client IssuerDN and Serial Number match the ones provided by the Realm, will return true");
        }
        else if (LOGGER.isTraceEnabled())
        {
            LOGGER.trace("Client IssuerDN ({}) or Serial Number ({}) do not match the one provided by the Realm ({} / {}), will return false",
                    new Object[] { toString(token.getIssuerDN()),
                            token.getHexSerialNumber(),
                            toString(info.getIssuerDN()),
                            info.getHexSerialNumber() });
        }
        
        return match;
    }
    
}
