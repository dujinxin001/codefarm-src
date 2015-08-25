package com.sxj.redis.provider.impl;

import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import com.sxj.redis.provider.RProvider;
import com.sxj.redis.provider.exception.RedisException;
import com.sxj.spring.modules.util.ClassLoaderUtil;

/**
 * Redis 缓存实现
 */
public class SingleRedisProvider implements RProvider
{
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
    
    private static String host;
    
    private static int port;
    
    private static int timeout;
    
    private static String password;
    
    private static int database;
    
    private static JedisPool pool;
    
    private String configFile;
    
    public SingleRedisProvider(String configFile)
    {
        this.configFile = configFile;
        initRedisProvider();
    }
    
    @Override
    public String name()
    {
        return "redis";
    }
    
    public void initRedisProvider()
    {
        try
        {
            InputStream configStream = ClassLoaderUtil.getResource(configFile);
            Properties props = new Properties();
            props.load(configStream);
            configStream.close();
            start(getProviderProperties(props));
        }
        catch (Exception e)
        {
            throw new RedisException("Unabled to initialize cache providers", e);
        }
    }
    
    private final Properties getProviderProperties(Properties props)
    {
        Properties tmp = new Properties();
        Enumeration<Object> keys = props.keys();
        String prefix = "redis.collections.";
        while (keys.hasMoreElements())
        {
            String key = (String) keys.nextElement();
            if (key.startsWith(prefix))
                tmp.setProperty(key.substring(prefix.length()),
                        props.getProperty(key));
        }
        return tmp;
    }
    
    /**
     * 释放资源
     * @param jedis
     * @param broken
     */
    public void returnResource(Jedis jedis, boolean broken)
    {
        if (null == jedis)
            return;
        if (broken)
        {
            pool.returnBrokenResource(jedis);
        }
        else
            pool.returnResource(jedis);
    }
    
    public Jedis getResource(String key)
    {
        return pool.getResource();
    }
    
    @Override
    public void start(Properties props) throws RedisException
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
        
    }
    
    @Override
    public void stop()
    {
        pool.destroy();
    }
    
    private static String getProperty(Properties props, String key,
            String defaultValue)
    {
        return props.getProperty(key, defaultValue).trim();
    }
    
    private static int getProperty(Properties props, String key,
            int defaultValue)
    {
        try
        {
            return Integer.parseInt(props.getProperty(key,
                    String.valueOf(defaultValue)).trim());
        }
        catch (NumberFormatException e)
        {
            return defaultValue;
        }
    }
    
    private static boolean getProperty(Properties props, String key,
            boolean defaultValue)
    {
        return "true".equalsIgnoreCase(props.getProperty(key,
                String.valueOf(defaultValue)).trim());
    }
}
