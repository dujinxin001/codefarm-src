package com.sxj.redis.core.pubsub;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.collections.MapUtils;

import redis.clients.jedis.Jedis;

import com.sxj.redis.core.MessageListener;
import com.sxj.redis.core.RTopic;
import com.sxj.redis.core.exception.RedisException;
import com.sxj.redis.core.impl.RedisObject;
import com.sxj.redis.provider.RProvider;

public class RedisTopic<M> extends RedisObject implements RTopic<M>
{
    
    private Map<Integer, TopicThread<M>> pubsubs = new HashMap<Integer, TopicThread<M>>();
    
    public RedisTopic(RProvider provider, String name)
    {
        super(provider, name);
    }
    
    @Override
    public long publish(M message)
    {
        
        Jedis jedis = provider.getResource(name);
        boolean broken = false;
        try
        {
            return jedis.publish(name, V_SERIALIZER.serialize(message));
        }
        catch (Exception e)
        {
            broken = true;
            throw new RedisException("", e);
        }
        finally
        {
            provider.returnResource(jedis, broken);
        }
    }
    
    @Override
    public int addListener(final MessageListener<M> listener)
    {
        Jedis jedis = provider.getResource(name);
        try
        {
            RedisMessageListenerWrapper<M> wrapper = new RedisMessageListenerWrapper<M>(
                    listener, name);
            TopicThread<M> topicThread = new TopicThread<M>(provider, wrapper);
            ExecutorService newCachedThreadPool = Executors.newFixedThreadPool(1);
            topicThread.setExecutor(newCachedThreadPool);
            int hashCode = wrapper.hashCode();
            if (!pubsubs.containsKey(hashCode))
            {
                pubsubs.put(hashCode, topicThread);
                newCachedThreadPool.execute(topicThread);
            }
            return hashCode;
        }
        catch (Exception e)
        {
            provider.returnResource(jedis, true);
            throw new RedisException("", e);
        }
    }
    
    @Override
    public void removeListener(int listenerId)
    {
        Jedis jedis = null;
        try
        {
            synchronized (pubsubs)
            {
                if (pubsubs.containsKey(listenerId))
                {
                    TopicThread<M> thread = pubsubs.get(listenerId);
                    jedis = thread.getJedis();
                    thread.getExecutor().shutdown();
                    pubsubs.remove(listenerId);
                    thread.getWrapper().unsubscribe(name);
                    if (MapUtils.isEmpty(pubsubs))
                        provider.returnResource(jedis, false);
                }
            }
            
        }
        catch (Exception e)
        {
            if (jedis != null)
                provider.returnResource(jedis, true);
            throw new RedisException("", e);
        }
    }
    
    public class TopicThread<M> implements Runnable
    {
        
        private RedisMessageListenerWrapper<M> wrapper;
        
        private RProvider provider;
        
        private Jedis jedis;
        
        private ExecutorService executor;
        
        private static final int MILLIS_TO_RETRY = 1000;
        
        public TopicThread(RProvider provider,
                RedisMessageListenerWrapper<M> wrapper)
        {
            this.provider = provider;
            this.wrapper = wrapper;
            jedis = provider.getResource(name);
        }
        
        @Override
        public void run()
        {
            try
            {
                if (jedis != null)
                    jedis.subscribe(wrapper, wrapper.getChannel());
            }
            catch (Exception e)
            {
                provider.returnResource(jedis, true);
                refreshJedis();
            }
        }
        
        private void refreshJedis()
        {
            try
            {
                Thread.sleep(MILLIS_TO_RETRY);
                jedis = provider.getResource(name);
                executor.execute(this);
            }
            catch (Exception e)
            {
                refreshJedis();
            }
        }
        
        private RedisMessageListenerWrapper<M> getWrapper()
        {
            return wrapper;
        }
        
        public ExecutorService getExecutor()
        {
            return executor;
        }
        
        public void setExecutor(ExecutorService executor)
        {
            this.executor = executor;
        }
        
        private Jedis getJedis()
        {
            return jedis;
        }
        
    }
    
}
