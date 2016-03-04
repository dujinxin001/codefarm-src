package com.codefarm.redis.core.pubsub;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codefarm.redis.core.MessageListener;
import com.codefarm.spring.modules.util.Serializers;
import com.codefarm.spring.modules.util.serializer.Serializer;

import redis.clients.jedis.JedisPubSub;

public class RedisMessageListenerWrapper<M> extends JedisPubSub
{
    private static final Logger LOGGER = LoggerFactory
            .getLogger(RedisMessageListenerWrapper.class);
    
    private MessageListener<M> listener;
    
    private String channel;
    
    protected final static Serializer V_SERIALIZER = Serializers
            .getJdkSerializer();
    
    public RedisMessageListenerWrapper(MessageListener<M> listener,
            String channel)
    {
        super();
        this.listener = listener;
        this.channel = channel;
    }
    
    @Override
    public void onMessage(String channel, String message)
    {
        try
        {
            listener.onMessage((M) V_SERIALIZER.deserialize(message));
        }
        catch (Exception e)
        {
            LOGGER.debug(message, e);
        }
    }
    
    @Override
    public void onPMessage(String pattern, String channel, String message)
    {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void onSubscribe(String channel, int subscribedChannels)
    {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void onUnsubscribe(String channel, int subscribedChannels)
    {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void onPUnsubscribe(String pattern, int subscribedChannels)
    {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void onPSubscribe(String pattern, int subscribedChannels)
    {
        // TODO Auto-generated method stub
        
    }
    
    public String getChannel()
    {
        return channel;
    }
}
