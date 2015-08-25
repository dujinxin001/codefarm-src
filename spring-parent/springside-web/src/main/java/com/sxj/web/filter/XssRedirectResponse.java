package com.sxj.web.filter;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class XssRedirectResponse extends HttpServletResponseWrapper
{
    private HttpServletResponse response = null;
    
    private HttpServletRequest request;
    
    public XssRedirectResponse(HttpServletResponse response,
            HttpServletRequest request)
    {
        super(response);
        this.response = response;
        this.request = request;
    }
    
    @Override
    public void sendRedirect(String location) throws IOException
    {
        String sanitizedLocation = location.replaceAll("(\\r|\\n|%0D|%0A|%0a|%0d)",
                "");
        
        if (!sanitizedLocation.contains("://"))
        {
            
            StringBuilder sb = new StringBuilder();
            
            if (!sanitizedLocation.startsWith("/"))
            {
                sb.append(request.getContextPath() + "/" + sanitizedLocation);
            }
            else
            {
                sb.append(sanitizedLocation);
            }
            
            response.sendRedirect(sb.toString());
        }
        else
        {
            response.sendRedirect(sanitizedLocation);
        }
    }
    
}
