package com.sxj.web.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.sxj.web.WebGuardException;

public final class SessionAttribute extends AbstractAction
{
    
    private static final long serialVersionUID = 1367492926060283228L;
    
    @Override
    public void execute(HttpServletRequest request,
            HttpServletResponse response, WebGuardException csrfe)
            throws WebGuardException
    {
        String attributeName = getParameter("AttributeName");
        HttpSession session = request.getSession(false);
        
        if (session != null)
        {
            session.setAttribute(attributeName, csrfe);
        }
    }
    
}
