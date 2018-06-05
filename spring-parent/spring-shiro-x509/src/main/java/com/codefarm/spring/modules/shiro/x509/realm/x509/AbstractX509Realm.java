package com.codefarm.spring.modules.shiro.x509.realm.x509;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.realm.AuthorizingRealm;

import com.codefarm.spring.modules.shiro.x509.authc.x509.X509AuthenticationInfo;
import com.codefarm.spring.modules.shiro.x509.authc.x509.X509AuthenticationToken;

public abstract class AbstractX509Realm extends AuthorizingRealm
{
    
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(
            AuthenticationToken token) throws AuthenticationException
    {
        return doGetX509AuthenticationInfo((X509AuthenticationToken) token);
    }
    
    protected abstract X509AuthenticationInfo doGetX509AuthenticationInfo(
            X509AuthenticationToken x509AuthenticationToken);
    
}
