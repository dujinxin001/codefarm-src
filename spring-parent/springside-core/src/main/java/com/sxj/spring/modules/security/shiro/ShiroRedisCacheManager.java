package com.sxj.spring.modules.security.shiro;

import org.apache.shiro.cache.AbstractCacheManager;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;

import com.sxj.cache.manager.CacheLevel;

public class ShiroRedisCacheManager extends AbstractCacheManager
{
    
    private CacheLevel level = CacheLevel.REDIS;
    
    //认证
    public static final String authenticationCacheName = "shiro-authenticationCacheName";
    
    //授权
    public static final String authorizationCacheName = "shiro-authorizationCacheName";
    
    @Override
    protected Cache createCache(String cacheName) throws CacheException
    {
        return new ShiroRedisCache<String, Object>(getLevel(), cacheName);
    }
    
    public CacheLevel getLevel()
    {
        return level;
    }
    
    public void setLevel(CacheLevel level)
    {
        this.level = level;
    }
    
}
