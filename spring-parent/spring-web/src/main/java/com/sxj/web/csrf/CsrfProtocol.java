package com.sxj.web.csrf;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sxj.web.Protocol;

public abstract class CsrfProtocol implements Protocol
{
    protected static final String securityTokenKey = CsrfGuard.getInstance()
            .getSessionKey();
    
    protected CsrfProtocol next;
    
    public abstract boolean wasViolated(HttpServletRequest request,
            HttpServletResponse response);
    
    public abstract String rejectionCause();
    
    protected boolean doNext(HttpServletRequest request,
            HttpServletResponse response)
    {
        if (next != null)
        {
            return next.wasViolated(request, response);
        }
        return true;
    }
}
