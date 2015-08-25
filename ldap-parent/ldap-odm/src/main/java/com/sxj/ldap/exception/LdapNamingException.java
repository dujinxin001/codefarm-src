package com.sxj.ldap.exception;

import javax.naming.NamingException;

public class LdapNamingException extends RuntimeException
{
    public LdapNamingException(final Throwable namingException)
    {
        super(namingException);
    }
    
    public LdapNamingException(final NamingException namingException)
    {
        super(namingException.getExplanation(), namingException);
    }
    
    public LdapNamingException(final String userMessage,
            final Throwable namingException)
    {
        super(namingException);
    }
    
    public LdapNamingException(final String message)
    {
        super(message);
    }
}
