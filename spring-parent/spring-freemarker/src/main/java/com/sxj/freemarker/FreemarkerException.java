package com.sxj.freemarker;

public class FreemarkerException extends RuntimeException
{
    
    public FreemarkerException()
    {
        super();
    }
    
    public FreemarkerException(String message, Throwable cause,
            boolean enableSuppression, boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }
    
    public FreemarkerException(String message, Throwable cause)
    {
        super(message, cause);
    }
    
    public FreemarkerException(String message)
    {
        super(message);
    }
    
    public FreemarkerException(Throwable cause)
    {
        super(cause);
    }
    
}
