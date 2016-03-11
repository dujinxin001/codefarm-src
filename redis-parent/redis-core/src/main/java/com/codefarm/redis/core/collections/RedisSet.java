package com.codefarm.redis.core.collections;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import redis.clients.jedis.Jedis;

import com.codefarm.redis.core.RSet;
import com.codefarm.redis.core.exception.RedisException;
import com.codefarm.redis.core.impl.RedisExpirable;
import com.codefarm.redis.provider.RProvider;

public class RedisSet<V> extends RedisExpirable implements RSet<V>
{
    
    public RedisSet(RProvider provider, String name)
    {
        super(provider, name);
    }
    
    @Override
    public int size()
    {
        Jedis jedis = provider.getResource(name);
        boolean broken = false;
        try
        {
            return jedis.scard(name).intValue();
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
    public boolean isEmpty()
    {
        return size() == 0;
    }
    
    @Override
    public boolean contains(Object o)
    {
        Jedis jedis = provider.getResource(name);
        boolean broken = false;
        try
        {
            return jedis.sismember(name, V_SERIALIZER.serialize(o));
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
    public Iterator<V> iterator()
    {
        return new RedisSetIterator<V>(provider, this);
    }
    
    @Override
    public Object[] toArray()
    {
        Jedis jedis = provider.getResource(name);
        boolean broken = false;
        try
        {
            Set<String> smembers = jedis.smembers(name);
            return smembers.toArray();
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
    public <T> T[] toArray(T[] a)
    {
        Jedis jedis = provider.getResource(name);
        boolean broken = false;
        try
        {
            Set<String> smembers = jedis.smembers(name);
            return smembers.toArray(a);
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
    public boolean add(V e)
    {
        Jedis jedis = provider.getResource(name);
        boolean broken = false;
        try
        {
            int sadd = jedis.sadd(name, V_SERIALIZER.serialize(e)).intValue();
            switch (sadd)
            {
                case 1:
                    return true;
                default:
                    return false;
            }
        }
        catch (Exception ex)
        {
            broken = true;
            throw new RedisException("", ex);
        }
        finally
        {
            provider.returnResource(jedis, broken);
        }
    }
    
    @Override
    public boolean remove(Object o)
    {
        Jedis jedis = provider.getResource(name);
        boolean broken = false;
        try
        {
            int sadd = jedis.srem(name, V_SERIALIZER.serialize(o)).intValue();
            switch (sadd)
            {
                case 1:
                    return true;
                default:
                    return false;
            }
        }
        catch (Exception ex)
        {
            broken = true;
            throw new RedisException("", ex);
        }
        finally
        {
            provider.returnResource(jedis, broken);
        }
    }
    
    @Override
    public boolean containsAll(Collection<?> c)
    {
        for (Object object : c)
        {
            if (!contains(object))
            {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public boolean addAll(Collection<? extends V> c)
    {
        Jedis jedis = provider.getResource(name);
        boolean broken = false;
        String[] values = array(c);
        try
        {
            int sadd = jedis.sadd(name, values).intValue();
            switch (sadd)
            {
                case 1:
                    return true;
                default:
                    return false;
            }
        }
        catch (Exception ex)
        {
            broken = true;
            throw new RedisException("", ex);
        }
        finally
        {
            provider.returnResource(jedis, broken);
        }
    }
    
    private String[] array(Collection<?> c)
    {
        String[] values = new String[c.size()];
        int index = 0;
        for (Object value : c)
        {
            values[index] = V_SERIALIZER.serialize(value);
            index++;
        }
        return values;
    }
    
    @Override
    public boolean retainAll(Collection<?> c)
    {
        boolean changed = false;
        for (Object object : this)
        {
            if (!c.contains(object))
            {
                remove(object);
                changed = true;
            }
        }
        return changed;
    }
    
    @Override
    public boolean removeAll(Collection<?> c)
    {
        Jedis jedis = provider.getResource(name);
        boolean broken = false;
        String[] values = array(c);
        try
        {
            int sadd = jedis.srem(name, values).intValue();
            switch (sadd)
            {
                case 1:
                    return true;
                default:
                    return false;
            }
        }
        catch (Exception ex)
        {
            broken = true;
            throw new RedisException("", ex);
        }
        finally
        {
            provider.returnResource(jedis, broken);
        }
    }
    
    @Override
    public void clear()
    {
        delete();
    }
    
}
