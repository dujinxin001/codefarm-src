package com.sxj.redis.core.collections;

import com.sxj.redis.core.RCollections;
import com.sxj.redis.core.RDeque;
import com.sxj.redis.core.RList;
import com.sxj.redis.core.RMap;
import com.sxj.redis.core.RQueue;
import com.sxj.redis.core.RSet;
import com.sxj.redis.provider.RProvider;
import com.sxj.redis.provider.impl.SingleRedisProvider;

public class RedisCollections implements RCollections
{
    private RProvider provider;
    
    public RedisCollections(String configFile)
    {
        provider = new SingleRedisProvider(configFile);
    }
    
    @Override
    public <K, V> RMap<K, V> getMap(String name)
    {
        return new RedisMap<K, V>(provider, name);
    }
    
    public <V> RSet<V> getSet(String name)
    {
        return new RedisSet<V>(provider, name);
    }
    
    @Override
    public <V> RList<V> getList(String name)
    {
        return new RedisList<V>(provider, name);
    }
    
    @Override
    public <V> RQueue<V> getQueue(String name)
    {
        return new RedisQueue<V>(provider, name);
    }
    
    @Override
    public <V> RDeque<V> getDeque(String name)
    {
        return new RedisDeque<V>(provider, name);
    }
    
}
