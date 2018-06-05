package com.sxj.web.csfr.protocol;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;

import junit.framework.Assert;

import org.junit.Test;

import com.sxj.web.csrf.MustHaveSecurityTokenProtocol;

/**
 * @author jonasabreu
 *  
 */
final public class MustHaveSecurityTokenProtocolTest
{
    
    @Test
    public void testThatIsViolatedIfCantFindTokenRequestParameter()
    {
        HttpServletRequest request = mock(HttpServletRequest.class);
        Assert.assertTrue(new MustHaveSecurityTokenProtocol().wasViolated(request,
                null));
    }
    
    @Test
    public void testThatIsNotViolatedIfFindsTokenRequestParameter()
    {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter("token")).thenReturn("");
        Assert.assertFalse(new MustHaveSecurityTokenProtocol().wasViolated(request,
                null));
    }
    
    @Test
    public void testRejectionCause()
    {
        Assert.assertEquals("Security token [security-token] is not present.",
                new MustHaveSecurityTokenProtocol().rejectionCause());
    }
}
