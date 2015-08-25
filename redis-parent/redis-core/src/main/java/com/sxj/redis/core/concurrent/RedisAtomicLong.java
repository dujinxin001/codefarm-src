package com.sxj.redis.core.concurrent;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import com.sxj.redis.core.RAtomicLong;
import com.sxj.redis.core.exception.RedisException;
import com.sxj.redis.core.impl.RedisExpirable;
import com.sxj.redis.provider.RProvider;

public class RedisAtomicLong extends RedisExpirable implements RAtomicLong
{
    
    public RedisAtomicLong(RProvider provider, String name)
    {
        super(provider, name);
        init();
    }
    
    public RedisAtomicLong(RProvider provider, String name, long seconds)
    {
        super(provider, name);
        init();
        this.expire(seconds, TimeUnit.SECONDS);
    }
    
    public RedisAtomicLong(RProvider provider, String name, Date timestamp)
    {
        super(provider, name);
        init();
        this.expireAt(timestamp);
    }
    
    private void init()
    {
        Jedis jedis = provider.getResource(name);
        boolean broken = false;
        try
        {
            jedis.setnx(name, "0");
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
    public long getAndDecrement()
    {
        return getAndAdd(1);
    }
    
    @Override
    public long addAndGet(long delta)
    {
        Jedis jedis = provider.getResource(name);
        boolean broken = false;
        try
        {
            return jedis.incrBy(name, 1);
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
    public boolean compareAndSet(long expect, long update)
    {
        Jedis jedis = provider.getResource(name);
        boolean broken = false;
        try
        {
            while (true)
            {
                Long value = Long.parseLong(jedis.get(name));
                if (value != expect)
                {
                    return false;
                }
                Transaction multi = jedis.multi();
                multi.set(getName(), String.valueOf(update));
                if (multi.exec().size() == 1)
                {
                    return true;
                }
            }
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
    public long decrementAndGet()
    {
        
        Jedis jedis = provider.getResource(name);
        boolean broken = false;
        try
        {
            return jedis.decrBy(name, 1);
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
    public Long get()
    {
        Jedis jedis = provider.getResource(name);
        boolean broken = false;
        try
        {
            Long value = Long.parseLong(jedis.get(name));
            return value;
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
    public long getAndAdd(long delta)
    {
        while (true)
        {
            long current = get();
            long next = current + delta;
            if (compareAndSet(current, next))
                return current;
        }
    }
    
    @Override
    public long getAndSet(long newValue)
    {
        Jedis jedis = provider.getResource(name);
        boolean broken = false;
        try
        {
            return Long.parseLong(jedis.getSet(name, String.valueOf(newValue)));
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
    public long incrementAndGet()
    {
        Jedis jedis = provider.getResource(name);
        boolean broken = false;
        try
        {
            return jedis.incr(name);
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
    public long getAndIncrement()
    {
        return getAndAdd(1);
    }
    
    @Override
    public void set(long newValue)
    {
        Jedis jedis = provider.getResource(name);
        boolean broken = false;
        try
        {
            jedis.set(name, String.valueOf(newValue));
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
