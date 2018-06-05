/**
 * The OWASP CSRFGuard Project, BSD License
 * Eric Sheridan (eric@infraredsecurity.com), Copyright (c) 2011 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *    2. Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *    3. Neither the name of OWASP nor the names of its contributors may be used
 *       to endorse or promote products derived from this software without specific
 *       prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.sxj.web.filter;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.sxj.web.csrf.CsrfGuard;
import com.sxj.web.csrf.CsrfProtocol;
import com.sxj.web.csrf.MustHaveSecurityTokenProtocol;
import com.sxj.web.util.Resources;
import com.sxj.web.util.Streams;

public final class CsrfFilter implements Filter
{
    private final static String CONFIG_PARAM = "configFile";
    
    private FilterConfig filterConfig = null;
    
    CsrfProtocol protocol = new MustHaveSecurityTokenProtocol();
    
    CsrfGuard csrfGuard = CsrfGuard.getInstance();
    
    @Override
    public void destroy()
    {
        filterConfig = null;
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain filterChain) throws IOException, ServletException
    {
        if (request instanceof HttpServletRequest
                && response instanceof HttpServletResponse)
        {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpSession session = httpRequest.getSession();
            
            if (session == null)
            {
                filterChain.doFilter(httpRequest,
                        (HttpServletResponse) response);
                return;
            }
            
            csrfGuard.getLogger()
                    .info(String.format("CsrfGuard analyzing request %s",
                            httpRequest.getRequestURI()));
            
            CsrfRedirectResponse httpResponse = new CsrfRedirectResponse(
                    (HttpServletResponse) response, httpRequest, csrfGuard);
            if (session.isNew())
            {
                csrfGuard.getTokenValue(session);
                filterChain.doFilter(httpRequest, httpResponse);
            }
            else if (!protocol.wasViolated(httpRequest, httpResponse))
            {
                
                filterChain.doFilter(httpRequest, httpResponse);
            }
            
        }
        else
        {
            filterChain.doFilter(request, response);
        }
    }
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException
    {
        this.filterConfig = filterConfig;
        String config = filterConfig.getInitParameter(CONFIG_PARAM);
        
        if (config == null)
        {
            throw new RuntimeException(
                    String.format("failure to specify context init-param - %s",
                            CONFIG_PARAM));
        }
        
        InputStream is = null;
        Properties properties = new Properties();
        
        try
        {
            is = Resources.getResourceStream(config,
                    filterConfig.getServletContext(),
                    CsrfFilter.class);
            properties.load(is);
            CsrfGuard.getInstance().load(properties);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
            Streams.close(is);
        }
    }
}
