package com.sxj.spring.modules.shiro.x509.authc.x509;

import javax.security.auth.x500.X500Principal;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractX509CredentialsMatcher implements
        CredentialsMatcher
{
    
    protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractX509CredentialsMatcher.class);
    
    @Override
    public final boolean doCredentialsMatch(AuthenticationToken token,
            AuthenticationInfo info)
    {
        return doX509CredentialsMatch((X509AuthenticationToken) token,
                (X509AuthenticationInfo) info);
    }
    
    public abstract boolean doX509CredentialsMatch(
            X509AuthenticationToken token, X509AuthenticationInfo info);
    
    protected final String toString(X500Principal dn)
    {
        return dn.getName(X500Principal.CANONICAL);
    }
    
    protected final boolean doEquals(X500Principal one, X500Principal other)
    {
        return toString(one).equals(toString(other));
    }
    
}
