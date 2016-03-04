package com.codefarm.redis.core.pubsub;

import java.util.HashMap;
import java.util.Map;

import com.codefarm.redis.core.RTopic;
import com.codefarm.redis.core.RTopics;
import com.codefarm.redis.provider.RProvider;
import com.codefarm.redis.provider.impl.SingleRedisProvider;

public class RedisTopics implements RTopics
{
    private RProvider provider;
    
    private static Map<String, RedisTopic> topics = new HashMap<String, RedisTopic>();
    
    public RedisTopics(String configFile)
    {
        provider = new SingleRedisProvider(configFile);
    }
    
    @Override
    public <M> RTopic<M> getTopic(String name)
    {
        if (topics.containsKey(name))
            return (RTopic<M>) topics.get(name);
        else
        {
            synchronized (topics)
            {
                RedisTopic<M> redisTopic = new RedisTopic<M>(provider, name);
                topics.put(name, redisTopic);
                return redisTopic;
            }
            
        }
    }
    
}
