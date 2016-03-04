package com.codefarm.redis.core.impl;

import com.codefarm.redis.core.RObject;
import com.codefarm.redis.core.exception.RedisException;
import com.codefarm.redis.provider.RProvider;
import com.codefarm.spring.modules.util.Serializers;
import com.codefarm.spring.modules.util.serializer.Serializer;

import redis.clients.jedis.Jedis;

public class RedisObject implements RObject
{
    
    protected RProvider provider;
    
    protected String name;
    
    protected final static Serializer K_SERIALIZER = Serializers
            .getJsonSerializer();
    
    protected final static Serializer V_SERIALIZER = Serializers
            .getJdkSerializer();
    
    public RedisObject(RProvider provider, String name)
    {
        super();
        this.provider = provider;
        this.name = name;
    }
    
    @Override
    public String getName()
    {
        return name;
    }
    
    @Override
    public void delete()
    {
        Jedis jedis = provider.getResource(name);
        boolean broken = false;
        try
        {
            jedis.del(getName());
        }
        catch (Exception e)
        {
            broken = true;
            throw new RedisException("", e);
        }
        finally
        {
            provider.returnResource(jedis, broken);
        }
    }
    
    public static Serializer getKSerializer()
    {
        return K_SERIALIZER;
    }
    
    public static Serializer getVSerializer()
    {
        return V_SERIALIZER;
    }
    
}
