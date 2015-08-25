package com.sxj.web.actions;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sxj.web.WebGuardException;

public final class Redirect extends AbstractAction
{
    
    private static final long serialVersionUID = -2265693822259717332L;
    
    @Override
    public void execute(HttpServletRequest request,
            HttpServletResponse response, WebGuardException csrfe)
            throws WebGuardException
    {
        String errorPage = getParameter("Page");
        
        try
        {
            response.sendRedirect(errorPage);
        }
        catch (IOException ioe)
        {
            throw new WebGuardException(ioe);
        }
    }
    
}
