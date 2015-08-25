package com.sxj.redis.core;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

/**
 * Distributed and concurrent implementation of {@link java.util.concurrent.ConcurrentMap}
 * and {@link java.util.Map}
 *
 *
 * @param <K> key
 * @param <V> value
 */
public interface RMap<K, V> extends ConcurrentMap<K, V>, RExpirable
{
    
    /**
     * Gets a map slice contains the mappings with defined <code>keys</code>
     * by one operation. This operation <b>NOT</b> traverses all map entries
     * like any other <code>filter*</code> method, so works faster.
     *
     * The returned map is <b>NOT</b> backed by the original map.
     *
     * @param keys map keys
     * @return
     */
    Map<K, V> getAll(Set<K> keys);
    
}
