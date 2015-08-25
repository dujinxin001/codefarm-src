package com.sxj.web.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sxj.web.WebGuardException;

public final class Log extends AbstractAction
{
    
    private static final long serialVersionUID = 8238761463376338707L;
    
    private Logger logger = LoggerFactory.getLogger(getClass());
    
    @Override
    public void execute(HttpServletRequest request,
            HttpServletResponse response, WebGuardException csrfe)
            throws WebGuardException
    {
        String logMessage = getParameter("Message");
        
        /** Exception Information **/
        logMessage = logMessage.replaceAll("%exception%", String.valueOf(csrfe));
        logMessage = logMessage.replaceAll("%exception_message%",
                csrfe.getLocalizedMessage());
        
        /** Remote Network Information **/
        logMessage = logMessage.replaceAll("%remote_ip%",
                request.getRemoteAddr());
        logMessage = logMessage.replaceAll("%remote_host%",
                request.getRemoteHost());
        logMessage = logMessage.replaceAll("%remote_port%",
                String.valueOf(request.getRemotePort()));
        
        /** Local Network Information **/
        logMessage = logMessage.replaceAll("%local_ip%", request.getLocalAddr());
        logMessage = logMessage.replaceAll("%local_host%",
                request.getLocalName());
        logMessage = logMessage.replaceAll("%local_port%",
                String.valueOf(request.getLocalPort()));
        
        /** Requested Resource Information **/
        logMessage = logMessage.replaceAll("%request_uri%",
                request.getRequestURI());
        logMessage = logMessage.replaceAll("%request_url%",
                request.getRequestURL().toString());
        
        /** JavaEE Principal Information **/
        if (request.getRemoteUser() != null)
        {
            logMessage = logMessage.replaceAll("%user%",
                    request.getRemoteUser());
        }
        else
        {
            logMessage = logMessage.replaceAll("%user%", "<anonymous>");
        }
        
        logger.error(logMessage);
    }
    
}
