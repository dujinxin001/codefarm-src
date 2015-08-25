package com.sxj.redis.core.concurrent;

import io.netty.util.concurrent.Promise;

import java.util.concurrent.Semaphore;

public class RedisLockEntry
{
    
    private int counter;
    
    private final Semaphore latch;
    
    private final Promise<Boolean> promise;
    
    public RedisLockEntry(RedisLockEntry source)
    {
        counter = source.counter;
        latch = source.latch;
        promise = source.promise;
    }
    
    public RedisLockEntry(Promise<Boolean> promise)
    {
        super();
        this.latch = new Semaphore(1);
        this.promise = promise;
    }
    
    public boolean isFree()
    {
        return counter == 0;
    }
    
    public void aquire()
    {
        counter++;
    }
    
    public void release()
    {
        counter--;
    }
    
    public Promise<Boolean> getPromise()
    {
        return promise;
    }
    
    public Semaphore getLatch()
    {
        return latch;
    }
    
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + counter;
        return result;
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RedisLockEntry other = (RedisLockEntry) obj;
        if (counter != other.counter)
            return false;
        return true;
    }
    
}
