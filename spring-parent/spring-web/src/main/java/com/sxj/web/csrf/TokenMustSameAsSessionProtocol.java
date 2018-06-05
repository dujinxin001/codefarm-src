package com.sxj.web.csrf;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

final public class TokenMustSameAsSessionProtocol extends CsrfProtocol
{
    
    public String rejectionCause()
    {
        return "Security token [" + securityTokenKey
                + "] dose not equal the value in session.";
    }
    
    public boolean wasViolated(HttpServletRequest request,
            HttpServletResponse response)
    {
        return !CsrfGuard.getInstance().isValidRequest(request, response);
    }
}
