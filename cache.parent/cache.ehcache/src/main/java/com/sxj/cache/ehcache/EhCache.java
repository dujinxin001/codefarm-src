package com.sxj.cache.ehcache;

import java.util.List;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.event.CacheEventListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sxj.cache.core.Cache;
import com.sxj.cache.core.CacheException;
import com.sxj.cache.core.CacheExpiredListener;

/**
 * EHCache
 */
public class EhCache implements Cache, CacheEventListener
{
    private static final Logger LOGGER = LoggerFactory.getLogger(Ehcache.class);
    
    private net.sf.ehcache.Cache cache;
    
    private CacheExpiredListener listener;
    
    /**
     * Creates a new Hibernate pluggable cache based on a cache name.
     * <p/>
     *
     * @param cache The underlying EhCache instance to use.
     */
    public EhCache(net.sf.ehcache.Cache cache, CacheExpiredListener listener)
    {
        this.cache = cache;
        this.cache.getCacheEventNotificationService().registerListener(this);
        this.listener = listener;
    }
    
    @SuppressWarnings("rawtypes")
    public List keys()
    {
        return this.cache.getKeys();
    }
    
    /**
     * Gets a value of an element which matches the given key.
     *
     * @param key the key of the element to return.
     * @return The value placed into the cache with an earlier put, or null if not found or expired
     * @
     */
    public Object get(Object key)
    {
        try
        {
            if (key == null)
                return null;
            else
            {
                Element element = cache.get(key);
                if (element != null)
                    return element.getObjectValue();
            }
            
        }
        catch (net.sf.ehcache.CacheException e)
        {
            LOGGER.error("Error occured when get data from L2 cache", e);
        }
        return null;
    }
    
    /**
     * Puts an object into the cache.
     *
     * @param key   a key
     * @param value a value
     * @ if the {@link CacheManager}
     *                        is shutdown or another {@link Exception} occurs.
     */
    public void update(Object key, Object value)
    {
        put(key, value);
    }
    
    /**
     * Puts an object into the cache.
     *
     * @param key   a key
     * @param value a value
     * @ if the {@link CacheManager}
     *                        is shutdown or another {@link Exception} occurs.
     */
    public void put(Object key, Object value)
    {
        try
        {
            Element element = new Element(key, value);
            cache.put(element);
        }
        catch (IllegalArgumentException e)
        {
            LOGGER.error("Error occured when get data from L2 cache", e);
        }
        catch (IllegalStateException e)
        {
            LOGGER.error("Error occured when get data from L2 cache", e);
        }
        catch (net.sf.ehcache.CacheException e)
        {
            LOGGER.error("Error occured when get data from L2 cache", e);
        }
        
    }
    
    /**
     * Removes the element which matches the key.
     * <p/>
     * If no element matches, nothing is removed and no Exception is thrown.
     *
     * @param key the key of the element to remove
     * @
     */
    @Override
    public void evict(Object key)
    {
        try
        {
            cache.remove(key);
        }
        catch (IllegalStateException e)
        {
            LOGGER.error("Error occured when get data from L2 cache", e);
        }
        catch (net.sf.ehcache.CacheException e)
        {
            LOGGER.error("Error occured when get data from L2 cache", e);
        }
    }
    
    /* (non-Javadoc)
     * @see net.oschina.j2cache.Cache#batchRemove(java.util.List)
     */
    @Override
    @SuppressWarnings("rawtypes")
    public void evict(List keys)
    {
        cache.removeAll(keys);
    }
    
    /**
     * Remove all elements in the cache, but leave the cache
     * in a useable state.
     *
     * @
     */
    public void clear()
    {
        try
        {
            cache.removeAll();
        }
        catch (IllegalStateException e)
        {
            LOGGER.error("Error occured when get data from L2 cache", e);
        }
        catch (net.sf.ehcache.CacheException e)
        {
            LOGGER.error("Error occured when get data from L2 cache", e);
        }
    }
    
    /**
     * Remove the cache and make it unuseable.
     *
     * @
     */
    public void destroy()
    {
        try
        {
            cache.getCacheManager().removeCache(cache.getName());
        }
        catch (IllegalStateException e)
        {
            LOGGER.error("Error occured when get data from L2 cache", e);
        }
        catch (net.sf.ehcache.CacheException e)
        {
            LOGGER.error("Error occured when get data from L2 cache", e);
        }
    }
    
    @Override
    public void dispose()
    {
        return;
    }
    
    @Override
    public void notifyElementEvicted(Ehcache arg0, Element arg1)
    {
        return;
    }
    
    @Override
    public void notifyElementExpired(Ehcache cache, Element elem)
    {
        if (listener != null)
        {
            listener.notifyElementExpired(cache.getName(), elem.getObjectKey());
        }
    }
    
    @Override
    public void notifyElementPut(Ehcache arg0, Element arg1)
    
    {
        return;
    }
    
    @Override
    public void notifyElementRemoved(Ehcache arg0, Element arg1)
    
    {
        return;
    }
    
    @Override
    public void notifyElementUpdated(Ehcache arg0, Element arg1)
    
    {
        return;
    }
    
    @Override
    public void notifyRemoveAll(Ehcache arg0)
    {
        return;
    }
    
    @Override
    public Long size()
    {
        return (long) keys().size();
    }
    
    @Override
    public List values()
    {
        throw new CacheException("Operation not supported!!!");
    }
    
    @Override
    public void put(Object key, Object value, int seconds)
    
    {
        try
        {
            Element element = new Element(key, value);
            element.setTimeToLive(seconds);
            cache.put(element);
        }
        catch (IllegalArgumentException e)
        {
            LOGGER.error("Error occured when get data from L2 cache", e);
        }
        catch (IllegalStateException e)
        {
            LOGGER.error("Error occured when get data from L2 cache", e);
        }
        catch (net.sf.ehcache.CacheException e)
        {
            LOGGER.error("Error occured when get data from L2 cache", e);
        }
    }
    
    @Override
    public Boolean exists(Object key)
    {
        return cache.isKeyInCache(key);
    }
    
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        throw new CloneNotSupportedException();
    }
    
}