package com.codefarm.redis.core;

import java.util.Date;

public interface RConcurrent
{
    public RAtomicLong getAtomicLong(String name);
    
    public RAtomicLong getAtomicLong(String name, long seconds);
    
    public RAtomicLong getAtomicLong(String name, Date time);
}
