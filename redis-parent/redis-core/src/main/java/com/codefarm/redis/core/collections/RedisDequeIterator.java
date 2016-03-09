package com.codefarm.redis.core.collections;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class RedisDequeIterator<V> implements Iterator<V>
{
    private int currentIndex;
    
    private boolean removeExecuted;
    
    private RedisDeque<V> redisDeque;
    
    public RedisDequeIterator(RedisDeque<V> redisDeque)
    {
        super();
        this.redisDeque = redisDeque;
        currentIndex = redisDeque.size();
    }
    
    @Override
    public boolean hasNext()
    {
        int size = redisDeque.size();
        return currentIndex > 0 && size > 0;
    }
    
    @Override
    public V next()
    {
        if (!hasNext())
        {
            throw new NoSuchElementException("No such element at index "
                    + currentIndex);
        }
        currentIndex--;
        removeExecuted = false;
        return redisDeque.get(currentIndex);
    }
    
    @Override
    public void remove()
    {
        if (removeExecuted)
        {
            throw new IllegalStateException("Element been already deleted");
        }
        redisDeque.remove(currentIndex);
        currentIndex++;
        removeExecuted = true;
    }
    
}
