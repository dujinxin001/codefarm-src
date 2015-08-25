package com.sxj.redis.core.collections;

import java.util.ListIterator;
import java.util.NoSuchElementException;

public class RedisListIterator<V> implements ListIterator<V>
{
    private int currentIndex;
    
    private boolean removeExecuted;
    
    private RedisList<V> redisList;
    
    public RedisListIterator(int index, RedisList<V> redisList)
    {
        this.currentIndex = index - 1;
        this.redisList = redisList;
    }
    
    @Override
    public boolean hasNext()
    {
        int size = redisList.size();
        return currentIndex + 1 < size && size > 0;
    }
    
    @Override
    public V next()
    {
        if (!hasNext())
        {
            throw new NoSuchElementException("No such element at index "
                    + currentIndex);
        }
        currentIndex++;
        removeExecuted = false;
        return redisList.get(currentIndex);
    }
    
    @Override
    public void remove()
    {
        if (removeExecuted)
        {
            throw new IllegalStateException("Element been already deleted");
        }
        redisList.remove(currentIndex);
        currentIndex--;
        removeExecuted = true;
    }
    
    @Override
    public boolean hasPrevious()
    {
        int size = redisList.size();
        return currentIndex - 1 < size && size > 0 && currentIndex >= 0;
    }
    
    @Override
    public V previous()
    {
        if (!hasPrevious())
        {
            throw new NoSuchElementException("No such element at index "
                    + currentIndex);
        }
        removeExecuted = false;
        V res = redisList.get(currentIndex);
        currentIndex--;
        return res;
    }
    
    @Override
    public int nextIndex()
    {
        return currentIndex + 1;
    }
    
    @Override
    public int previousIndex()
    {
        return currentIndex;
    }
    
    @Override
    public void set(V e)
    {
        if (currentIndex >= redisList.size() - 1)
        {
            throw new IllegalStateException();
        }
        redisList.set(currentIndex, e);
    }
    
    @Override
    public void add(V e)
    {
        redisList.add(currentIndex + 1, e);
        currentIndex++;
    }
}
