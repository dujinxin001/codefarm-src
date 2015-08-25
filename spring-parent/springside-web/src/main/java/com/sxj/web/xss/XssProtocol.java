package com.sxj.web.xss;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sxj.web.csrf.CsrfProtocol;

public class XssProtocol extends CsrfProtocol
{
    
    @Override
    public boolean wasViolated(HttpServletRequest request,
            HttpServletResponse response)
    {
        return XssGuard.getInstance().isValidRequest(request, response);
    }
    
    @Override
    public String rejectionCause()
    {
        return null;
    }
    
}
