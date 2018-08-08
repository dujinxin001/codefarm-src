package com.codefarm.cache.core;

import java.util.List;
import java.util.Map;

public interface Cache
{
 
    public void publish(String name,String value);
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

	public Object brpop(Object key,int seconds);

	public void lpush(Object key, byte[] value);
	
	public byte[] rpop(Object key);

	public void incr(String key);

	public void putNoSeri(String key, String value);

	public void putNoSeri(String key, String value, int seconds);
	
	public Object getNoSeri(String key);

	public void zadd(String key, Map<String,Double> scoreMembers);

	public Object zrange(String key, int start, int end);

	public void zrem(String key, String member);
    
}
