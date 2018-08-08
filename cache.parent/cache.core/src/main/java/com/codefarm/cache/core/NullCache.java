package com.codefarm.cache.core;

import java.util.List;
import java.util.Map;

public class NullCache implements Cache
{
    
    /* (non-Javadoc)
     * @see net.oschina.j2cache.Cache#get(java.lang.Object)
     */
    @Override
    public Object get(Object key)
    {
        return null;
    }
    
    /* (non-Javadoc)
     * @see net.oschina.j2cache.Cache#put(java.lang.Object, java.lang.Object)
     */
    @Override
    public void put(Object key, Object value)
    {
        return;
    }
    
    /* (non-Javadoc)
     * @see net.oschina.j2cache.Cache#update(java.lang.Object, java.lang.Object)
     */
    @Override
    public void update(Object key, Object value)
    {
        return;
    }
    
    /* (non-Javadoc)
     * @see net.oschina.j2cache.Cache#keys()
     */
    @Override
    @SuppressWarnings("rawtypes")
    public List keys()
    {
        return null;
    }
    
    /* (non-Javadoc)
     * @see net.oschina.j2cache.Cache#remove(java.lang.Object)
     */
    @Override
    public void evict(Object key)
    {
        return;
    }
    
    /* (non-Javadoc)
     * @see net.oschina.j2cache.Cache#batchRemove(java.util.List)
     */
    @Override
    @SuppressWarnings("rawtypes")
    public void evict(List keys)
    {
        return;
    }
    
    /* (non-Javadoc)
     * @see net.oschina.j2cache.Cache#clear()
     */
    @Override
    public void clear()
    {
        return;
    }
    
    /* (non-Javadoc)
     * @see net.oschina.j2cache.Cache#destroy()
     */
    @Override
    public void destroy()
    {
        return;
    }
    
    @Override
    public Long size()
    {
        return 0L;
    }
    
    @Override
    public List values()
    {
        return null;
    }
    
    @Override
    public void put(Object key, Object value, int seconds)
    
    {
        return;
    }
    
    @Override
    public Boolean exists(Object key)
    {
        return false;
    }

	@Override
	public Object brpop(Object key,int seconds) {
		return null;
	}


	@Override
	public void lpush(Object key, byte[] value) {
		return ;
	}

	@Override
	public void incr(String key) {
		return ;
	}

	@Override
	public void putNoSeri(String key, String value) {
		return ;
		
	}

	@Override
	public void putNoSeri(String key, String value, int seconds) {
		return ;
		
	}

	@Override
	public Object getNoSeri(String key) {
		return null;
	}


	@Override
	public void zadd(String key, Map<String, Double> scoreMembers) {
		return ;
	}

	@Override
	public Object zrange(String key, int start, int end) {
		return null;
	}

	@Override
	public void zrem(String key, String member) {
		return ;
	}

	@Override
	public void publish(String name, String value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public byte[] rpop(Object key) {
		// TODO Auto-generated method stub
		return null;
	}
    
}
