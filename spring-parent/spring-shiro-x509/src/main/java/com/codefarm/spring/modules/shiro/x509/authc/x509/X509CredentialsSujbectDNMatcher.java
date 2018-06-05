package com.codefarm.spring.modules.shiro.x509.authc.x509;

public class X509CredentialsSujbectDNMatcher extends
        AbstractX509CredentialsMatcher
{
    
    @Override
    public boolean doX509CredentialsMatch(X509AuthenticationToken token,
            X509AuthenticationInfo info)
    {
        boolean match = doEquals(token.getSubjectDN(), info.getSubjectDN());
        
        if (match)
        {
            LOGGER.trace("Client SubjectDN match the one provided by the Realm, will return true");
        }
        else if (LOGGER.isTraceEnabled())
        {
            LOGGER.trace("Client SubjectDN ({}) do not match the one provided by the Realm ({}), will return false",
                    toString(token.getSubjectDN()),
                    toString(info.getIssuerDN()));
        }
        
        return match;
    }
    
}
