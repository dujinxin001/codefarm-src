package com.sxj.web.sqlinjection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sxj.web.csrf.CsrfProtocol;

public class DenySqlInjectionProtocol extends CsrfProtocol
{
    @Override
    public boolean wasViolated(HttpServletRequest request,
            HttpServletResponse response)
    {
        // TODO Auto-generated method stub
        return false;
    }
    
    @Override
    public String rejectionCause()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
}
