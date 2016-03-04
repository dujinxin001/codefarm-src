package com.codefarm.cache.redis;

import java.util.Properties;

import com.codefarm.cache.core.Cache;
import com.codefarm.cache.core.CacheException;
import com.codefarm.cache.core.CacheExpiredListener;
import com.codefarm.cache.core.CacheProvider;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Redis 缓存实现
 * @author Winter Lau
 */
public class RedisCacheProvider implements CacheProvider
{
    
    private static String host;
    
    private static final String LOCALHOST = "127.0.0.1";
    
    private static final int DEFAULT_PORT = 6379;
    
    private static final int DEFAULT_TIMEOUT = 2000;
    
    private static final int DEFAULT_DATABASE = 0;
    
    private static final int DEFAULT_MAXIDLE = 10;
    
    private static final int DEFAULT_MINIDLE = 5;
    
    private static final int DEFAULT_NUMTESTSPEREVICTIONRUN = 10;
    
    private static final int DEFAULT_TIMEBETWEENEVICTIONRUNSMILLIS = 10;
    
    private static final int DEFAULT_SOFTMINEVICTABLEIDLETIMEMILLIS = 10;
    
    private static final int DEFAULT_MINEVICTABLEIDLETIMEMILLIS = 1000;
    
    private static int port;
    
    private static int timeout;
    
    private static String password;
    
    private static int database;
    
    private static JedisPool pool;
    
    private static Properties redisProps;
    
    @Override
    public String name()
    {
        return "redis";
    }
    
    /**
     * 释放资源
     * @param jedis
     * @param isBrokenResource
     */
    public void returnResource(Jedis jedis, boolean isBrokenResource)
    {
        if (null == jedis)
            return;
        if (isBrokenResource)
        {
            pool.returnBrokenResource(jedis);
        }
        else
            pool.returnResource(jedis);
    }
    
    public Jedis getResource()
    {
        try
        {
            return pool.getResource();
        }
        catch (Exception e)
        {
            pool.destroy();
            start(redisProps);
            throw new CacheException(e);
        }
    }
    
    @Override
    public Cache buildCache(String regionName, boolean autoCreate,
            CacheExpiredListener listener)
    {
        return new RedisCache(regionName, this);
    }
    
    @Override
    public void start(Properties props)
    {
        
        JedisPoolConfig config = new JedisPoolConfig();
        
        host = getProperty(props, "host", LOCALHOST);
        password = props.getProperty("password", null);
        
        port = getProperty(props, "port", DEFAULT_PORT);
        
        timeout = getProperty(props, "timeout", DEFAULT_TIMEOUT);
        
        database = getProperty(props, "database", DEFAULT_DATABASE);
        
        config.setMaxIdle(getProperty(props, "maxIdle", DEFAULT_MAXIDLE));
        
        config.setMinIdle(getProperty(props, "minIdle", DEFAULT_MINIDLE));
        config.setTestWhileIdle(getProperty(props, "testWhileIdle", false));
        config.setTestOnBorrow(getProperty(props, "testOnBorrow", true));
        config.setTestOnReturn(getProperty(props, "testOnReturn", false));
        config.setNumTestsPerEvictionRun(getProperty(props,
                "numTestsPerEvictionRun",
                DEFAULT_NUMTESTSPEREVICTIONRUN));
        config.setMinEvictableIdleTimeMillis(getProperty(props,
                "minEvictableIdleTimeMillis",
                DEFAULT_MINEVICTABLEIDLETIMEMILLIS));
        config.setSoftMinEvictableIdleTimeMillis(getProperty(props,
                "softMinEvictableIdleTimeMillis",
                DEFAULT_SOFTMINEVICTABLEIDLETIMEMILLIS));
        config.setTimeBetweenEvictionRunsMillis(getProperty(props,
                "timeBetweenEvictionRunsMillis",
                DEFAULT_TIMEBETWEENEVICTIONRUNSMILLIS));
        pool = new JedisPool(config, host, port, timeout, password, database);
        redisProps = props;
        
    }
    
    @Override
    public void stop()
    {
        pool.destroy();
    }
    
    private String getProperty(Properties props, String key,
            String defaultValue)
    {
        return props.getProperty(key, defaultValue).trim();
    }
    
    private int getProperty(Properties props, String key, int defaultValue)
    {
        try
        {
            return Integer.parseInt(props
                    .getProperty(key, String.valueOf(defaultValue)).trim());
        }
        catch (NumberFormatException nfe)
        {
            return defaultValue;
        }
    }
    
    private boolean getProperty(Properties props, String key,
            boolean defaultValue)
    {
        return "true".equalsIgnoreCase(
                props.getProperty(key, String.valueOf(defaultValue)).trim());
    }
}
