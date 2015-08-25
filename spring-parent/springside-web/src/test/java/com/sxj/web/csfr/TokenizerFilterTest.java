package com.sxj.web.csfr;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.junit.Before;
import org.junit.Test;

import com.sxj.web.filter.CsrfFilter;

/**
 * 
 */
final public class TokenizerFilterTest
{
    
    private HttpServletRequest request;
    
    private HttpServletResponse response;
    
    private FilterChain chain;
    
    private HttpSession session;
    
    @Before
    public void setup()
    {
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        chain = mock(FilterChain.class);
        session = mock(HttpSession.class);
        
        when(request.getSession()).thenReturn(session);
    }
    
    @Test
    public void testThatRejectsRequestIfCantFindToken() throws Throwable
    {
        when(request.getParameter("security-token")).thenReturn(null);
        
        new CsrfFilter().doFilter(request, response, chain);
        
        verifyZeroInteractions(chain);
        verify(response).sendError(403, "Invalid security token.");
    }
    
    @Test
    public void testThatRejectsRequestIfTokenIsNotOnSession() throws Throwable
    {
        when(request.getParameter("security-token")).thenReturn("token");
        when(session.getAttribute("net.vidageek.tokenizer.token.name")).thenReturn(null);
        
        new CsrfFilter().doFilter(request, response, chain);
        
        verifyZeroInteractions(chain);
        verify(response).sendError(403, "Invalid security token.");
    }
}
