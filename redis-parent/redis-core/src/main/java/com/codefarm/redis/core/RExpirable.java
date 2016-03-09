package com.codefarm.redis.core;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public interface RExpirable extends RObject
{
    boolean expire(long timeToLive, TimeUnit timeUnit);
    
    boolean expireAt(long timestamp);
    
    boolean expireAt(Date timestamp);
    
    /**
     * Remove the existing timeout of object
     *
     * @return <code>true</code> if timeout was removed
     *         <code>false</code> if object does not exist or does not have an associated timeout
     */
    boolean clearExpire();
    
    /**
     * Remaining time to live of Redisson object that has a timeout
     *
     * @return time in seconds
     *          -2 if the key does not exist.
     *          -1 if the key exists but has no associated expire.
     */
    long ttl();
}
