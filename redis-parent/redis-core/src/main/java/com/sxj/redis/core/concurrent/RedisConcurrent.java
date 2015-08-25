package com.sxj.redis.core.concurrent;

import java.util.Date;

import com.sxj.redis.core.RAtomicLong;
import com.sxj.redis.core.RConcurrent;
import com.sxj.redis.provider.RProvider;
import com.sxj.redis.provider.impl.SingleRedisProvider;

public class RedisConcurrent implements RConcurrent
{
    private RProvider provider;
    
    public RedisConcurrent(String configFile)
    {
        provider = new SingleRedisProvider(configFile);
    }
    
    @Override
    public RAtomicLong getAtomicLong(String name)
    {
        return new RedisAtomicLong(provider, name);
    }
    
    @Override
    public RAtomicLong getAtomicLong(String name, long seconds)
    {
        return new RedisAtomicLong(provider, name, seconds);
    }
    
    @Override
    public RAtomicLong getAtomicLong(String name, Date time)
    {
        return new RedisAtomicLong(provider, name, time);
    }
    
}
