package com.sxj.redis.core.impl;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import redis.clients.jedis.Jedis;

import com.sxj.redis.core.RExpirable;
import com.sxj.redis.core.exception.RedisException;
import com.sxj.redis.provider.RProvider;

public class RedisExpirable extends RedisObject implements RExpirable
{
    
    public RedisExpirable(RProvider provider, String name)
    {
        super(provider, name);
    }
    
    @Override
    public boolean expire(long timeToLive, TimeUnit timeUnit)
    {
        Jedis jedis = provider.getResource(name);
        boolean broken = false;
        try
        {
            jedis.expire(name,
                    ((Long) timeUnit.toSeconds(timeToLive)).intValue());
            return true;
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
    
    @Override
    public boolean expireAt(long timestamp)
    {
        Jedis jedis = provider.getResource(name);
        boolean broken = false;
        try
        {
            jedis.expireAt(name, timestamp);
            return true;
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
    
    @Override
    public boolean expireAt(Date timestamp)
    {
        Jedis jedis = provider.getResource(name);
        boolean broken = false;
        try
        {
            jedis.expireAt(name, timestamp.getTime() / 1000);
            return true;
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
    
    @Override
    public boolean clearExpire()
    {
        Jedis jedis = provider.getResource(name);
        boolean broken = false;
        try
        {
            jedis.persist(name);
            return true;
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
    
    @Override
    public long ttl()
    {
        Jedis jedis = provider.getResource(name);
        boolean broken = false;
        try
        {
            return jedis.ttl(name);
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
    
}
