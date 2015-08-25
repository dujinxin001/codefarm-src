package com.sxj.spring.modules.security.shiro;

import java.io.Serializable;
import java.util.Collection;

import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.realm.AuthenticatingRealm;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.SessionListener;

public class ShiroSessionListener implements SessionListener
{
    DefaultSecurityManager securityManager;
    
    @Override
    public void onExpiration(Session arg0)
    {
        System.out.println("==========================================================session expired"
                + arg0.getId());
        clearCached(arg0.getId());
    }
    
    @Override
    public void onStart(Session arg0)
    {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void onStop(Session arg0)
    {
        clearCached(arg0.getId());
    }
    
    private void clearCached(Serializable sessionId)
    {
        CacheManager cacheManager = securityManager.getCacheManager();
        clearAuthenticationCache(cacheManager, sessionId);
    }
    
    private void clearAuthenticationCache(CacheManager cacheManager,
            Serializable sessionId)
    {
        Collection<Realm> realms = securityManager.getRealms();
        for (Realm realm : realms)
        {
            if (realm instanceof AuthorizingRealm)
            {
                cacheManager.getCache(((AuthorizingRealm) realm).getAuthorizationCacheName())
                        .clear();
                cacheManager.getCache(((AuthenticatingRealm) realm).getAuthenticationCacheName())
                        .remove(sessionId);
            }
            else if (realm instanceof AuthenticatingRealm)
                cacheManager.getCache(((AuthenticatingRealm) realm).getAuthenticationCacheName())
                        .remove(sessionId);
        }
    }
    
    public void setSecurityManager(DefaultSecurityManager securityManager)
    {
        this.securityManager = securityManager;
    }
    
}
