package com.sxj.web.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.sxj.web.WebGuardException;

public final class Invalidate extends AbstractAction
{
    
    private static final long serialVersionUID = -3060679616261531773L;
    
    @Override
    public void execute(HttpServletRequest request,
            HttpServletResponse response, WebGuardException csrfe)
            throws WebGuardException
    {
        HttpSession session = request.getSession(false);
        
        if (session != null)
        {
            session.invalidate();
        }
    }
    
}
