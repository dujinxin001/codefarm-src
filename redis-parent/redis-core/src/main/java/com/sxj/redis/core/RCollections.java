package com.sxj.redis.core;

public interface RCollections
{
    
    /**
     * Returns map instance by name.
     *
     * @param name of map
     * @return
     */
    <K, V> RMap<K, V> getMap(String name);
    
    /**
     * Returns set instance by name.
     *
     * @param name of map
     * @return
     */
    <V> RSet<V> getSet(String name);
    
    /**
     * Returns list instance by name.
     *
     * @param name of list
     * @return
     */
    <V> RList<V> getList(String name);
    
    /**
     * Returns queue instance by name.
     *
     * @param name of queue
     * @return
     */
    <V> RQueue<V> getQueue(String name);
    
    /**
     * Returns deque instance by name.
     *
     * @param name of deque
     * @return
     */
    <V> RDeque<V> getDeque(String name);
    
}
