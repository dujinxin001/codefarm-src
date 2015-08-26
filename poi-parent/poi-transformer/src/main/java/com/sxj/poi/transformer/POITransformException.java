package com.sxj.poi.transformer;

public class POITransformException extends Exception
{
    
    public POITransformException()
    {
        super();
    }
    
    public POITransformException(String message, Throwable cause,
            boolean enableSuppression, boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }
    
    public POITransformException(String message, Throwable cause)
    {
        super(message, cause);
    }
    
    public POITransformException(String message)
    {
        super(message);
    }
    
    public POITransformException(Throwable cause)
    {
        super(cause);
    }
    
}
