package com.codefarm.ldap.exception;

import javax.naming.NamingException;

@SuppressWarnings({ "JavaDoc" })
public class LpaAnnotationException extends LdapNamingException
{
    public LpaAnnotationException(final Throwable namingException)
    {
        super(namingException);
    }
    
    public LpaAnnotationException(final NamingException namingException)
    {
        super(namingException);
    }
    
    public LpaAnnotationException(final String userMessage,
            final Throwable namingException)
    {
        super(userMessage, namingException);
    }
    
    public LpaAnnotationException(final String message)
    {
        super(message);
    }
}
