package com.sxj.redis.core.collections;

import java.util.NoSuchElementException;

import redis.clients.jedis.Jedis;

import com.sxj.redis.core.RQueue;
import com.sxj.redis.core.exception.RedisException;
import com.sxj.redis.provider.RProvider;

public class RedisQueue<V> extends RedisList<V> implements RQueue<V>
{
    public RedisQueue(RProvider provider, String name)
    {
        super(provider, name);
    }
    
    @Override
    public boolean offer(V e)
    {
        return add(e);
    }
    
    @Override
    public V remove()
    {
        V value = poll();
        if (value == null)
        {
            throw new NoSuchElementException();
        }
        return value;
    }
    
    @Override
    public V poll()
    {
        Jedis jedis = provider.getResource(name);
        boolean broken = false;
        try
        {
            String lpop = jedis.lpop(name);
            return (V) V_SERIALIZER.deserialize(lpop);
        }
        catch (Exception e)
        {
            broken = true;
            throw new RedisException(e);
        }
        finally
        {
            provider.returnResource(jedis, broken);
        }
    }
    
    @Override
    public V element()
    {
        Jedis jedis = provider.getResource(name);
        boolean broken = false;
        try
        {
            String lindex = jedis.lindex(name, 0);
            if (lindex == null)
            {
                throw new NoSuchElementException();
            }
            return (V) V_SERIALIZER.serialize(lindex);
        }
        catch (Exception e)
        {
            broken = true;
            throw new RedisException(e);
        }
        finally
        {
            provider.returnResource(jedis, broken);
        }
    }
    
    @Override
    public V peek()
    {
        if (isEmpty())
        {
            return null;
        }
        return get(0);
    }
    
}
