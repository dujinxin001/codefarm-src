package com.sxj.redis.core;

import org.junit.Before;
import org.junit.Test;

import com.sxj.redis.core.concurrent.RedisConcurrent;

public class RedisAtomicLongTest
{
    RedisConcurrent concurrent;
    
    private static final String LIST_NAME = "test-list";
    
    @Before
    public void setUp()
    {
        concurrent = new RedisConcurrent("config/redis-collections.properties");
    }
    
    @Test
    public void test()
    {
        RAtomicLong atomicLong = concurrent.getAtomicLong("A", 10);
        atomicLong.incrementAndGet();
    }
    
}
