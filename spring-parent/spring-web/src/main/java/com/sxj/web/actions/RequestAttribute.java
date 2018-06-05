package com.sxj.web.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sxj.web.WebGuardException;

public final class RequestAttribute extends AbstractAction
{
    
    private static final long serialVersionUID = 6714855990116387348L;
    
    @Override
    public void execute(HttpServletRequest request,
            HttpServletResponse response, WebGuardException csrfe)
            throws WebGuardException
    {
        String attributeName = getParameter("AttributeName");
        
        request.setAttribute(attributeName, csrfe);
    }
    
}
