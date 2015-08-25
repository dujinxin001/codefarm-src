package com.sxj.cache.core;

import java.util.Properties;

public interface CacheProvider
{
    
    /**
     * 缓存的标识名称
     * @return
     */
    public String name();
    
    /**
     * Configure the cache
     *
     * @param regionName the name of the cache region
     * @param autoCreate autoCreate settings
     * @param listener listener for expired elements
     * @
     */
    public Cache buildCache(String regionName, boolean autoCreate,
            CacheExpiredListener listener);
    
    /**
     * Callback to perform any necessary initialization of the underlying cache implementation
     * during SessionFactory construction.
     *
     * @param properties current configuration settings.
     */
    public void start(Properties props);
    
    /**
     * Callback to perform any necessary cleanup of the underlying cache implementation
     * during SessionFactory.close().
     */
    public void stop();
    
}
