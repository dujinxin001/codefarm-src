package com.sxj.web.csrf;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 
 */
final public class MustHaveSecurityTokenProtocol extends CsrfProtocol
{
    
    //    public MustHaveSecurityTokenProtocol(final String securityTokenName)
    //    {
    //        this.securityTokenName = securityTokenName;
    //    }
    
    public boolean wasViolated(HttpServletRequest request,
            HttpServletResponse response)
    {
        return request.getParameter(securityTokenKey) == null
                && doNext(request, response);
    }
    
    public MustHaveSecurityTokenProtocol()
    {
        next = new TokenMustBeOnSessionProtocol();
    }
    
    public String rejectionCause()
    {
        return "Security token [" + securityTokenKey + "] is not present.";
    }
    
}
