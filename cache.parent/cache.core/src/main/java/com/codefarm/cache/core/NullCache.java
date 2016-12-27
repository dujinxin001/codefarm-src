package com.codefarm.cache.core;

import java.util.List;

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
	public void lpush(Object key, Object value) {
		return ;
	}

	@Override
	public void incr(Object key) {
		return ;
	}
    
}
