package com.sxj.web.csrf;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @author jonasabreu
 * 
 */
final public class TokenMustBeOnSessionProtocol extends CsrfProtocol
{
    
    public TokenMustBeOnSessionProtocol()
    {
        next = new TokenMustSameAsSessionProtocol();
    }
    
    public String rejectionCause()
    {
        return "Security token [" + securityTokenKey
                + "] is not present in session.";
    }
    
    public boolean wasViolated(HttpServletRequest request,
            HttpServletResponse response)
    {
        HttpSession session = request.getSession(false);
        if (session == null)
            return false;
        return session.getAttribute(securityTokenKey) == null
                && doNext(request, response);
    }
    
}
