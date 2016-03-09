package com.codefarm.mybatis.cache;

import java.util.concurrent.locks.ReadWriteLock;

import org.apache.ibatis.cache.Cache;

import com.codefarm.cache.manager.CacheLevel;
import com.codefarm.cache.manager.HierarchicalCacheManager;

public class HierarchicalCache implements Cache
{
    
    //    private static final CacheManager CACHE_MANAGER = CacheManager.create();
    
    private String cacheId;
    
    private int level = 0;
    
    private int timeToLive = 5;
    
    public HierarchicalCache(String cacheId)
    {
        //        if (cacheId == null)
        //        {
        //            throw new IllegalArgumentException("Cache instances require an ID");
        //        }
        //        if (!CACHE_MANAGER.cacheExists(cacheId))
        //        {
        //            CACHE_MANAGER.addCache(cacheId);
        //        }
        //        this.cache = CACHE_MANAGER.getCache(cacheId);
        this.cacheId = cacheId;
    }
    
    @Override
    public String getId()
    {
        return this.cacheId;
    }
    
    @Override
    public void putObject(Object key, Object value)
    {
        putObject(level, key, value, timeToLive);
    }
    
    private void putObject(int level, Object key, Object value, int seconds)
    {
        HierarchicalCacheManager.evict(getLevel(level), cacheId, key);
        HierarchicalCacheManager.set(getLevel(level),
                this.cacheId,
                key,
                value,
                seconds);
        if ((level - 1) > 0)
            putObject(level - 1, key, value, seconds);
    }
    
    @Override
    public Object getObject(Object key)
    {
        return getObject(1, key);
    }
    
    private Object getObject(int level, Object key)
    {
        Object object = HierarchicalCacheManager.get(getLevel(level),
                this.cacheId,
                key);
        if (object != null)
        {
            putObject(level - 1, key, object, timeToLive);
            return object;
        }
        if ((level + 1) <= this.level)
            return getObject(level + 1, key);
        return null;
    }
    
    @Override
    public Object removeObject(Object key)
    {
        removeObject(level, key);
        return null;
    }
    
    private void removeObject(int level, Object key)
    {
        HierarchicalCacheManager.evict(getLevel(level), cacheId, key);
        if ((level - 1) > 0)
            removeObject(level - 1, key);
    }
    
    private void clear(int level)
    {
        HierarchicalCacheManager.clear(getLevel(level), cacheId);
        if ((level - 1) > 0)
            clear(level - 1);
    }
    
    @Override
    public void clear()
    {
        clear(level);
    }
    
    @Override
    public int getSize()
    {
        // TODO Auto-generated method stub
        return 0;
    }
    
    @Override
    public ReadWriteLock getReadWriteLock()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    public String getCacheId()
    {
        return cacheId;
    }
    
    public void setCacheId(String cacheId)
    {
        this.cacheId = cacheId;
    }
    
    public int getLevel()
    {
        return level;
    }
    
    public void setLevel(int level)
    {
        this.level = level;
    }
    
    public int getTimeToLive()
    {
        return timeToLive;
    }
    
    public void setTimeToLive(int timeToLive)
    {
        this.timeToLive = timeToLive;
    }
    
    private static CacheLevel getLevel(int level)
    {
        return CacheLevel.values()[level];
    }
}
