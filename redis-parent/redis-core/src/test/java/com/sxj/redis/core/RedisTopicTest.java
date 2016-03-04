package com.sxj.redis.core;

import org.junit.Before;
import org.junit.Test;

import com.codefarm.redis.core.MessageListener;
import com.codefarm.redis.core.RTopic;
import com.codefarm.redis.core.pubsub.RedisTopics;

public class RedisTopicTest
{
    RedisTopics topics;
    
    private static final String TOPIC_NAME = "topic1";
    
    @Before
    public void setUp()
    {
        topics = new RedisTopics("config/redis-collections.properties");
    }
    
    @Test
    public void test() throws InterruptedException
    {
        RTopic<Object> topic = topics.getTopic(TOPIC_NAME);
        
        int listener1 = topic.addListener(new MessageListener<Object>()
        {
            
            @Override
            public void onMessage(Object message)
            {
                System.out.println("message received: " + message);
            }
        });
        //		int listener2 = topic.addListener(new MessageListener<Object>() {
        //
        //			@Override
        //			public void onMessage(Object message) {
        //				System.out.println("=================: " + message);
        //			}
        //		});
        //		Thread.currentThread().sleep(1000);
        //		topic.publish("ABCDE測試中文");
        //		topic.publish("ABCDEFG");
        //		topic.removeListener(listener1);
        //		topic.publish("-------------------");
        while (true)
        {
            Thread.currentThread().sleep(1000);
        }
    }
}
