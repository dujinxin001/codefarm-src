package com.codefarm.redis.core.exception;

@SuppressWarnings("serial")
public class RedisException extends RuntimeException
{
    
    public RedisException()
    {
    }
    
    public RedisException(String msg)
    {
        super(msg);
    }
    
    public RedisException(String msg, Throwable e)
    {
        super(msg, e);
    }
    
    public RedisException(Throwable cause)
    {
        super(cause);
    }
}
