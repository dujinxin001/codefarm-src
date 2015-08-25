package com.sxj.redis.provider;

import java.util.Properties;

import redis.clients.jedis.Jedis;

public interface RProvider
{
    
    /**
     * 缓存的标识名称
     * @return
     */
    public String name();
    
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
    
    public void returnResource(Jedis jedis, boolean isBrokenResource);
    
    public Jedis getResource(String key);
    
}
