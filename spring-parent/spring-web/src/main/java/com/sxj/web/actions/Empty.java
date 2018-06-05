package com.sxj.web.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sxj.web.WebGuardException;

public final class Empty extends AbstractAction
{
    
    private static final long serialVersionUID = 3530383602177340966L;
    
    @Override
    public void execute(HttpServletRequest request,
            HttpServletResponse response, WebGuardException csrfe)
            throws WebGuardException
    {
        // nothing to do
    }
    
}
