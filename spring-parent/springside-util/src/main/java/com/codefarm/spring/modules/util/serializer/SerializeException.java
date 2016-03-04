package com.codefarm.spring.modules.util.serializer;

public class SerializeException extends RuntimeException
{
    
    public SerializeException(String message, Throwable cause)
    {
        super(message, cause);
    }
    
    public SerializeException(Throwable cause)
    {
        super(cause);
    }
    
}
