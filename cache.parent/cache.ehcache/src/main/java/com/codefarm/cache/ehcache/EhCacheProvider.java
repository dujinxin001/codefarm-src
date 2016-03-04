package com.codefarm.cache.ehcache;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codefarm.cache.core.CacheException;
import com.codefarm.cache.core.CacheExpiredListener;
import com.codefarm.cache.core.CacheProvider;

import net.sf.ehcache.CacheManager;

public class EhCacheProvider implements CacheProvider
{
    
    private static final Logger LOGGER = LoggerFactory
            .getLogger(EhCacheProvider.class);
    
    private static final String CONFIG_XML = "ehcache.xml";
    
    private CacheManager manager;
    
    private Map<String, EhCache> cacheManager;
    
    @Override
    public String name()
    {
        return "ehcache";
    }
    
    /**
     * Builds a Cache.
     * <p>
     * Even though this method provides properties, they are not used.
     * Properties for EHCache are specified in the ehcache.xml file.
     * Configuration will be read from ehcache.xml for a cache declaration
     * where the name attribute matches the name parameter in this builder.
     *
     * @param name the name of the cache. Must match a cache configured in ehcache.xml
     * @param properties not used
     * @return a newly built cache will be built and initialised
     * @throws CacheException inter alia, if a cache of the same name already exists
     */
    @Override
    public EhCache buildCache(String name, boolean autoCreate,
            CacheExpiredListener listener)
    {
        EhCache ehcache = cacheManager.get(name);
        synchronized (cacheManager)
        {
            
            if (ehcache == null && autoCreate)
            {
                
                ehcache = getEhCache(name, listener);
            }
        }
        return ehcache;
    }
    
    private EhCache getEhCache(String name, CacheExpiredListener listener)
    {
        net.sf.ehcache.Cache cache = manager.getCache(name);
        if (cache == null)
        {
            LOGGER.warn("Could not find configuration [" + name
                    + "]; using defaults.");
            manager.addCache(name);
            cache = manager.getCache(name);
            LOGGER.debug("started EHCache region: " + name);
        }
        EhCache ehcache = new EhCache(cache, listener);
        cacheManager.put(name, ehcache);
        return ehcache;
    }
    
    /**
     * Callback to perform any necessary initialization of the underlying cache implementation
     * during SessionFactory construction.
     *
     * @param properties current configuration settings.
     * @throws FileNotFoundException 
     */
    @Override
    public void start(Properties props)
    {
        if (manager != null)
        {
            LOGGER.warn(
                    "Attempt to restart an already started EhCacheProvider. Use sessionFactory.close() "
                            + " between repeated calls to buildSessionFactory. Using previously created EhCacheProvider."
                            + " If this behaviour is required, consider using net.sf.ehcache.hibernate.SingletonEhCacheProvider.");
            return;
        }
        if (props != null)
            LOGGER.info("Properties file not supported in this version!!");
        try
        {
            InputStream resource = com.codefarm.spring.modules.util.ClassLoaderUtil
                    .getResourceAsStream(CONFIG_XML);
            if (resource == null)
                throw new CacheException("cannot find ehcache.xml !!!");
            manager = new CacheManager(resource);
            cacheManager = new ConcurrentHashMap<String, EhCache>();
        }
        catch (FileNotFoundException e)
        {
            throw new CacheException(e);
        }
        catch (IOException e)
        {
            throw new CacheException(e);
        }
        
    }
    
    /**
     * Callback to perform any necessary cleanup of the underlying cache implementation
     * during SessionFactory.close().
     */
    @Override
    public void stop()
    {
        if (manager != null)
        {
            manager.shutdown();
            manager = null;
        }
    }
    
}
