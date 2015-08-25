package com.sxj.web.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.sxj.spring.modules.util.Identities;
import com.sxj.web.WebGuardException;
import com.sxj.web.csrf.CsrfGuard;

public class Rotate extends AbstractAction
{
    
    private static final long serialVersionUID = -3164557586544451406L;
    
    @Override
    public void execute(HttpServletRequest request,
            HttpServletResponse response, WebGuardException csrfe)
            throws WebGuardException
    {
        HttpSession session = request.getSession(false);
        
        if (session != null)
        {
            CsrfGuard csrfGuard = CsrfGuard.getInstance();
            updateSessionToken(session, csrfGuard);
            
            if (csrfGuard.isTokenPerPageEnabled())
            {
                updatePageTokens(session, csrfGuard);
            }
        }
    }
    
    private void updateSessionToken(HttpSession session, CsrfGuard csrfGuard)
            throws WebGuardException
    {
        String token = null;
        
        try
        {
            token = Identities.randomBase62(csrfGuard.getTokenLength());
        }
        catch (Exception e)
        {
            throw new WebGuardException(
                    String.format("unable to generate the random token - %s",
                            e.getLocalizedMessage()), e);
        }
        
        session.setAttribute(csrfGuard.getSessionKey(), token);
    }
    
    private void updatePageTokens(HttpSession session, CsrfGuard csrfGuard)
            throws WebGuardException
    {
        @SuppressWarnings("unchecked")
        Map<String, String> pageTokens = (Map<String, String>) session.getAttribute(CsrfGuard.PAGE_TOKENS_KEY);
        List<String> pages = new ArrayList<String>();
        
        if (pageTokens != null)
        {
            pages.addAll(pageTokens.keySet());
        }
        
        for (String page : pages)
        {
            String token = null;
            
            try
            {
                token = Identities.randomBase62(csrfGuard.getTokenLength());
            }
            catch (Exception e)
            {
                throw new WebGuardException(
                        String.format("unable to generate the random token - %s",
                                e.getLocalizedMessage()), e);
            }
            
            pageTokens.put(page, token);
        }
    }
    
}
