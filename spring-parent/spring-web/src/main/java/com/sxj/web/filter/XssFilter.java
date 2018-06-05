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

import com.sxj.web.util.Resources;
import com.sxj.web.util.Streams;
import com.sxj.web.xss.XssGuard;
import com.sxj.web.xss.XssProtocol;

public class XssFilter implements Filter
{
    XssProtocol protocol = new XssProtocol();
    
    private final static String CONFIG_PARAM = "configFile";
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException
    {
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
            XssGuard.getInstance().load(properties);
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
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException
    {
        if (request instanceof HttpServletRequest
                && response instanceof HttpServletResponse)
        {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            XssRedirectResponse httpResponse = new XssRedirectResponse(
                    (HttpServletResponse) response, httpRequest);
            if (!protocol.wasViolated(httpRequest, httpResponse))
            {
                chain.doFilter(httpRequest, httpResponse);
            }
        }
    }
    
    @Override
    public void destroy()
    {
        
    }
    
}
