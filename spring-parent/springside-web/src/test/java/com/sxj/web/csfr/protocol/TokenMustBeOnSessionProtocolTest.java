package com.sxj.web.csfr.protocol;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import junit.framework.Assert;

import org.junit.Test;

import com.sxj.web.csrf.TokenMustBeOnSessionProtocol;

/**
 * @author jonasabreu
 * 
 */
final public class TokenMustBeOnSessionProtocolTest
{
    
    @Test
    public void testThatRejectsRequestIfTokenIsNotOnSession()
    {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(session.getAttribute("token")).thenReturn(null);
        
        Assert.assertTrue(new TokenMustBeOnSessionProtocol().wasViolated(request,
                response));
    }
    
    @Test
    public void testThatAcceptsRequestIfTokenIsOnSession()
    {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(session.getAttribute("token")).thenReturn("value");
        
        Assert.assertFalse(new TokenMustBeOnSessionProtocol().wasViolated(request,
                response));
    }
    
    @Test
    public void testRejectedCause()
    {
        
        Assert.assertEquals(new TokenMustBeOnSessionProtocol().rejectionCause(),
                "Security token [token] is not present in session.");
        
    }
}
