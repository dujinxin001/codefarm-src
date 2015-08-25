package com.sxj.redis.core.collections;

import io.netty.util.concurrent.Future;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;

import com.sxj.redis.core.RSortedSet;
import com.sxj.redis.core.impl.RedisObject;
import com.sxj.redis.provider.RProvider;

public class RedisSortedSet<V> extends RedisObject implements RSortedSet<V>
{
    public RedisSortedSet(RProvider provider, String name)
    {
        super(provider, name);
    }
    
    @Override
    public Comparator<? super V> comparator()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public SortedSet<V> subSet(V fromElement, V toElement)
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public SortedSet<V> headSet(V toElement)
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public SortedSet<V> tailSet(V fromElement)
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public V first()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public V last()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public int size()
    {
        // TODO Auto-generated method stub
        return 0;
    }
    
    @Override
    public boolean isEmpty()
    {
        // TODO Auto-generated method stub
        return false;
    }
    
    @Override
    public boolean contains(Object o)
    {
        // TODO Auto-generated method stub
        return false;
    }
    
    @Override
    public Iterator<V> iterator()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public Object[] toArray()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public <T> T[] toArray(T[] a)
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public boolean add(V e)
    {
        // TODO Auto-generated method stub
        return false;
    }
    
    @Override
    public boolean remove(Object o)
    {
        // TODO Auto-generated method stub
        return false;
    }
    
    @Override
    public boolean containsAll(Collection<?> c)
    {
        // TODO Auto-generated method stub
        return false;
    }
    
    @Override
    public boolean addAll(Collection<? extends V> c)
    {
        // TODO Auto-generated method stub
        return false;
    }
    
    @Override
    public boolean retainAll(Collection<?> c)
    {
        // TODO Auto-generated method stub
        return false;
    }
    
    @Override
    public boolean removeAll(Collection<?> c)
    {
        // TODO Auto-generated method stub
        return false;
    }
    
    @Override
    public void clear()
    {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public Future<Boolean> addAsync(V value)
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public Future<Boolean> removeAsync(V value)
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public boolean trySetComparator(Comparator<? super V> comparator)
    {
        // TODO Auto-generated method stub
        return false;
    }
    
}
