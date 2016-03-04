package com.sxj.cache.manager;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import com.codefarm.cache.manager.CacheLevel;
import com.codefarm.cache.manager.HierarchicalCacheManager;

public class CacheManagerTest
{
    
    private HierarchicalCacheManager cacheManager;
    
    @Before
    public void setUp()
    {
        cacheManager = new HierarchicalCacheManager();
        cacheManager.setConfigFile("cache.properties");
        cacheManager.setDatabaseId("1");
        cacheManager.initCacheProvider();
    }
    
    public void testL1Cache()
    {
        cacheManager.set(CacheLevel.EHCACHE,
                "testL1Cache",
                "demokey",
                "demovalue");
        Object object = cacheManager.get(CacheLevel.EHCACHE,
                "testL1Cache",
                "demokey");
        Assert.assertEquals("demovalue", object.toString());
    }
    
    @Test
    public void testL2Cache()
    {
        //        List<String> result = new ArrayList<String>();
        //        result.add("a");
        //        result.add("b");
        //        result.add("c");
        //        result.add("d");
        //        result.add("e");
        cacheManager.set(CacheLevel.REDIS, "L2List", "ListString", "demo", 1000);
        System.out.println(cacheManager.get(CacheLevel.REDIS,
                "L2List",
                "ListString"));
        //        List<String> object = (List<String>) cacheManager.get(2,
        //                "L2List",
        //                "ListString");
        //        for (String obj : object)
        //        {
        //            System.out.println(obj);
        //        }
        //        //        cacheManager.set(2, "testL2Cache", "demokey2", "demovalue");
        //        Object object = cacheManager.get(2, "testL2Cache", "demokey");
        //        Assert.assertEquals("demovalue2", object.toString());
    }
}
