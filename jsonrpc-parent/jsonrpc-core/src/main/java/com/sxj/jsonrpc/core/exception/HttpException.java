package com.sxj.jsonrpc.core.exception;

import java.io.IOException;

public class HttpException extends RuntimeException
{
    private static final long serialVersionUID = 1L;
    
    public HttpException(String message, IOException cause)
    {
        super(message, cause);
    }
}
