package com.codefarm.cache.core;

import java.util.List;

public interface Cache
{
    
    /**
     * Get an item from the cache, nontransactionally
     * @param key
     * @return the cached object or <tt>null</tt>
     * @
     */
    public Object get(Object key);
    
    /**
     * Add an item to the cache, nontransactionally, with
     * failfast semantics
     * @param key
     * @param value
     * @
     */
    public void put(Object key, Object value);
    
    public void put(Object key, Object value, int seconds);
    
    /**
     * Add an item to the cache
     * @param key
     * @param value
     * @
     */
    public void update(Object key, Object value);
    
    @SuppressWarnings("rawtypes")
    public List keys();
    
    /**
     * Remove an item from the cache
     */
    public void evict(Object key);
    
    /**
     * Batch remove cache objects
     * @param keys
     * @
     */
    @SuppressWarnings("rawtypes")
    public void evict(List keys);
    
    /**
     * Clear the cache
     */
    public void clear();
    
    /**
     * Clean up
     */
    public void destroy();
    
    public Long size();
    
    public List values();
    
    public Boolean exists(Object key);
    
}
