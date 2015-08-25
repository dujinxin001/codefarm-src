package com.sxj.web;

public class WebGuardException extends Exception
{
    
    private static final long serialVersionUID = -4468336915273168914L;
    
    public WebGuardException(String msg)
    {
        super(msg);
    }
    
    public WebGuardException(Exception e)
    {
        super(e);
    }
    
    public WebGuardException(String msg, Exception e)
    {
        super(msg, e);
    }
    
}
