package com.sxj.jsonrpc.core.exception;

import java.io.IOException;

@SuppressWarnings("serial")
public class StreamEndedException extends IOException
{
    
    /**
     * 
     */
    public StreamEndedException()
    {
    }
    
    /**
     * @param message
     */
    public StreamEndedException(String message)
    {
        super(message);
    }
    
    /**
     * @param cause
     */
    public StreamEndedException(Throwable cause)
    {
        super(cause);
    }
    
    /**
     * @param message
     * @param cause
     */
    public StreamEndedException(String message, Throwable cause)
    {
        super(message, cause);
    }
    
}
