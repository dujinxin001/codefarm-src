package com.codefarm.cache.core;

public interface CacheExpiredListener
{
    
    /**
     * 当缓存中的某个对象超时被清除的时候触发
     * @param region
     * @param key
     */
    public void notifyElementExpired(String region, Object key);
    
}
