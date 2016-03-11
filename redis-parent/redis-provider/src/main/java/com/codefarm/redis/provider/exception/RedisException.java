package com.codefarm.redis.provider.exception;

public class RedisException extends RuntimeException
{
    
    private static final long serialVersionUID = 1L;
    
    public RedisException(String message, Throwable cause)
    {
        super(message, cause);
    }
    
    public RedisException(Throwable cause)
    {
        super(cause);
    }
    
}
