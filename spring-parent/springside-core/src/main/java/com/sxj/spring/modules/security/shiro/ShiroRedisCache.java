package com.sxj.spring.modules.security.shiro;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sxj.cache.manager.CacheLevel;
import com.sxj.cache.manager.HierarchicalCacheManager;

public class ShiroRedisCache<K, V> implements Cache<K, V>
{
    Logger logger = LoggerFactory.getLogger(getClass());
    
    private CacheLevel level = CacheLevel.REDIS;
    
    private String name = "shiroSessions";
    
    public ShiroRedisCache(CacheLevel level, String name)
    {
        super();
        this.level = level;
        this.name = name;
    }
    
    @Override
    public V get(K key) throws CacheException
    {
        logger.debug("根据key从Redis中获取对象 key [" + key + "]");
        try
        {
            if (key == null)
            {
                return null;
            }
            else
            {
                V value = (V) HierarchicalCacheManager.get(level, name, key);
                return value;
            }
        }
        catch (Throwable t)
        {
            throw new CacheException(t);
        }
        
    }
    
    @Override
    public V put(K key, V value) throws CacheException
    {
        logger.debug("根据key从存储 key [" + key + "]");
        try
        {
            HierarchicalCacheManager.set(level, name, key, value);
            return value;
        }
        catch (Throwable t)
        {
            throw new CacheException(t);
        }
    }
    
    @Override
    public V remove(K key) throws CacheException
    {
        logger.debug("从redis中删除 key [" + key + "]");
        try
        {
            V previous = get(key);
            HierarchicalCacheManager.evict(level, name, key);
            return previous;
        }
        catch (Throwable t)
        {
            throw new CacheException(t);
        }
    }
    
    @Override
    public void clear() throws CacheException
    {
        logger.debug("从redis中删除所有元素");
        try
        {
            HierarchicalCacheManager.clear(level, name);
        }
        catch (Throwable t)
        {
            throw new CacheException(t);
        }
    }
    
    @Override
    public int size()
    {
        try
        {
            Long longSize = HierarchicalCacheManager.size(level, name);
            return longSize.intValue();
        }
        catch (Throwable t)
        {
            throw new CacheException(t);
        }
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public Set<K> keys()
    {
        try
        {
            Set<K> keys = new HashSet<K>();
            keys.addAll(HierarchicalCacheManager.keys(level, name));
            return keys;
        }
        catch (Throwable t)
        {
            throw new CacheException(t);
        }
    }
    
    @Override
    public Collection<V> values()
    {
        try
        {
            Collection<V> values = HierarchicalCacheManager.values(level, name);
            return values;
        }
        catch (Throwable t)
        {
            throw new CacheException(t);
        }
    }
    
    public String getName()
    {
        return name;
    }
    
    public void setName(String name)
    {
        this.name = name;
    }
    
    private void setLevel(CacheLevel level)
    {
        this.level = level;
    }
    
}
