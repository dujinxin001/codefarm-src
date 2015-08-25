package com.sxj.spring.modules.shiro.x509.web;

import java.security.cert.X509Certificate;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.shiro.ShiroException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sxj.spring.modules.shiro.x509.authc.x509.X509AuthenticationToken;

public class X509AuthenticationFilter extends AuthenticatingFilter
{
    
    private static final Logger LOGGER = LoggerFactory.getLogger(X509AuthenticationFilter.class);
    
    @Override
    protected boolean onAccessDenied(ServletRequest request,
            ServletResponse response) throws Exception
    {
        return executeLogin(request, response);
    }
    
    @Override
    protected AuthenticationToken createToken(ServletRequest request,
            ServletResponse response) throws Exception
    {
        X509Certificate[] clientCertChain = (X509Certificate[]) request.getAttribute("javax.servlet.request.X509Certificate");
        LOGGER.info("X509AuthFilter.createToken() cert chain is {}",
                clientCertChain);
        if (clientCertChain == null || clientCertChain.length < 1)
        {
            throw new ShiroException(
                    "Request do not contain any X509Certificate");
        }
        return new X509AuthenticationToken(clientCertChain, getHost(request));
    }
    
}
