package com.sxj.redis.core;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.sxj.redis.core.collections.RedisCollections;

public class RedisSetTest
{
    RedisCollections collections;
    
    private static final String SET_NAME = "test-set";
    
    @Before
    public void setUp()
    {
        collections = new RedisCollections(
                "config/redis-collections.properties");
    }
    
    public void testAdd()
    {
        RSet<String> set = collections.getSet(SET_NAME);
        set.add("A");
        set.add("B");
    }
    
    public void testSize()
    {
        RSet<String> set = collections.getSet(SET_NAME);
        System.out.println(set.size());
    }
    
    public void testContains()
    {
        RSet<String> set = collections.getSet(SET_NAME);
        Assert.assertTrue(set.contains("A"));
    }
    
    public void testContainsAll()
    {
        RSet<String> set = collections.getSet(SET_NAME);
        List<String> values = new ArrayList<String>();
        values.add("A");
        Assert.assertTrue(set.containsAll(values));
    }
    
    public void testRemove()
    {
        RSet<String> set = collections.getSet(SET_NAME);
        set.remove("A");
        Assert.assertEquals(1, set.size());
    }
    
    public void testRemoveAll()
    {
        RSet<String> set = collections.getSet(SET_NAME);
        List<String> values = new ArrayList<String>();
        values.add("A");
        set.removeAll(values);
        Assert.assertEquals(1, set.size());
    }
    
    public void testExpire()
    {
        RSet<String> set = collections.getSet(SET_NAME);
        set.expire(10, TimeUnit.SECONDS);
    }
    
    public void testSetMap()
    {
        RSet<Map<String, String>> set = collections.getSet("MAP");
        Map<String, String> map = new HashMap<String, String>();
        map.put("A", "B");
        set.add(map);
    }
    
    @Test
    public void testIterator()
    {
        RSet<Map<String, String>> set = collections.getSet("MAP");
        for (int i = 1; i <= 20; i++)
        {
            Map<String, String> map = new HashMap<String, String>();
            map.put("A_" + i, "B_" + i);
            set.add(map);
        }
        
        Iterator<Map<String, String>> iterator = set.iterator();
        while (iterator.hasNext())
        {
            Map<String, String> next = iterator.next();
            Set<String> keySet = next.keySet();
            for (String key : keySet)
                System.out.println(key + ":" + next.get(key));
            System.out.println("----------------");
        }
    }
    
    public void testExpireAt()
    {
        RSet<String> set = collections.getSet(SET_NAME);
        set.add("A");
        Date now = new Date();
        Calendar instance = Calendar.getInstance();
        instance.setTime(now);
        instance.add(Calendar.MINUTE, 10);
        set.expireAt(instance.getTime());
    }
}
