package com.sxj.redis.core;

/**
 * Base interface for all Redisson objects
 *
 *
 */
public interface RObject
{
    
    /**
     * Returns name of object
     *
     * @return name
     */
    String getName();
    
    /**
     * Deletes the object
     */
    void delete();
    
}
