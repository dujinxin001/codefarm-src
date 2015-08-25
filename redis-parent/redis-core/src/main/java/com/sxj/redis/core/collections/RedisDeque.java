package com.sxj.redis.core.collections;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import redis.clients.jedis.Jedis;

import com.sxj.redis.core.RDeque;
import com.sxj.redis.core.exception.RedisException;
import com.sxj.redis.provider.RProvider;

public class RedisDeque<V> extends RedisQueue<V> implements RDeque<V>
{
    
    public RedisDeque(RProvider provider, String name)
    {
        super(provider, name);
    }
    
    @Override
    public void addFirst(V e)
    {
        add(e);
    }
    
    @Override
    public void addLast(V e)
    {
        Jedis jedis = provider.getResource(name);
        boolean broken = false;
        try
        {
            jedis.lpush(name, V_SERIALIZER.serialize(e));
        }
        catch (Exception ex)
        {
            broken = true;
            throw new RedisException(ex);
        }
        finally
        {
            provider.returnResource(jedis, broken);
        }
    }
    
    @Override
    public boolean offerFirst(V e)
    {
        Jedis jedis = provider.getResource(name);
        boolean broken = false;
        try
        {
            Long lpush = jedis.lpush(name, V_SERIALIZER.serialize(e));
            return lpush > 0;
        }
        catch (Exception ex)
        {
            broken = true;
            throw new RedisException(ex);
        }
        finally
        {
            provider.returnResource(jedis, broken);
        }
    }
    
    @Override
    public boolean offerLast(V e)
    {
        return offer(e);
    }
    
    @Override
    public V removeLast()
    {
        Jedis jedis = provider.getResource(name);
        boolean broken = false;
        try
        {
            String rpop = jedis.rpop(name);
            if (rpop == null)
            {
                throw new NoSuchElementException();
            }
            return (V) V_SERIALIZER.deserialize(rpop);
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
    public V pollFirst()
    {
        return poll();
    }
    
    @Override
    public V pollLast()
    {
        Jedis jedis = provider.getResource(name);
        boolean broken = false;
        try
        {
            String rpop = jedis.rpop(name);
            return (V) V_SERIALIZER.deserialize(rpop);
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
    public V getFirst()
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
            return (V) V_SERIALIZER.deserialize(lindex);
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
    public V getLast()
    {
        Jedis jedis = provider.getResource(name);
        boolean broken = false;
        try
        {
            List<String> lrange = jedis.lrange(name, -1, -1);
            if (lrange.isEmpty())
            {
                throw new NoSuchElementException();
            }
            return (V) V_SERIALIZER.deserialize(lrange.get(0));
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
    public V peekFirst()
    {
        return peek();
    }
    
    @Override
    public V peekLast()
    {
        Jedis jedis = provider.getResource(name);
        boolean broken = false;
        try
        {
            List<String> lrange = jedis.lrange(name, -1, -1);
            return (V) V_SERIALIZER.deserialize(lrange.get(0));
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
    public boolean removeFirstOccurrence(Object o)
    {
        return remove(o, 1);
    }
    
    @Override
    public boolean removeLastOccurrence(Object o)
    {
        return remove(o, -1);
    }
    
    @Override
    public void push(V e)
    {
        addFirst(e);
        
    }
    
    @Override
    public V pop()
    {
        return removeFirst();
    }
    
    @Override
    public Iterator<V> descendingIterator()
    {
        return new RedisDequeIterator<V>(this);
    }
    
    @Override
    public V removeFirst()
    {
        V value = poll();
        if (value == null)
        {
            throw new NoSuchElementException();
        }
        return value;
    }
    
}
