package com.sxj.redis.core;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.codefarm.redis.core.RMap;
import com.codefarm.redis.core.collections.RedisCollections;

public class RedisMapTest
{
    RedisCollections collections;
    
    private static final String MAP_NAME = "test-map";
    
    @Before
    public void setUp()
    {
        collections = new RedisCollections(
                "config/redis-collections.properties");
        testPut();
    }
    
    public void testPut()
    {
        RMap<String, String> map = collections.getMap(MAP_NAME);
        for (int i = 1; i <= 20; i++)
        {
            map.put("demo_" + i, "string_" + i);
        }
    }
    
    public void testGet()
    {
        
        RMap<String, String> map = collections.getMap(MAP_NAME);
        for (int i = 1; i <= 20; i++)
        {
            String value = map.get("demo_" + i);
            System.out.println(value);
        }
    }
    
    public void testSize()
    {
        RMap<String, List<String>> map = collections.getMap(MAP_NAME);
        map.size();
        org.junit.Assert.assertEquals(1, map.size());
    }
    
    public void testKeyset()
    {
        RMap<String, List<String>> map = collections.getMap(MAP_NAME);
        Set<String> keySet = map.keySet();
        Assert.assertEquals(1, keySet.size());
        Assert.assertTrue(keySet.contains("demo"));
    }
    
    public void testExpire()
    {
        RMap<String, List<String>> map = collections.getMap(MAP_NAME);
        map.expire(10, TimeUnit.SECONDS);
    }
    
    public void testTTL()
    {
        RMap<String, List<String>> map = collections.getMap(MAP_NAME);
        System.out.println(map.ttl());
    }
    
    @Test
    public void testOp()
    {
        RMap<Object, Object> map = collections.getMap("test-map2");
        Assert.assertTrue(map.isEmpty());
        map.put("a", "ab");
        map.put("b", "bc");
        Assert.assertFalse(map.isEmpty());
        Assert.assertEquals(2, map.size());
        Assert.assertTrue(map.containsKey("a"));
        Assert.assertTrue(map.containsValue("ab"));
        //        map.remove("a");
        //        map.remove("b", "bc");
        map.replace("a", "ab", "abc");
        System.out.println(map.get("a"));
        //        map.delete();
    }
    
}
