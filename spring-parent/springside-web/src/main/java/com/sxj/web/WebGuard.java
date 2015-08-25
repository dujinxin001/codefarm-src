package com.sxj.web;

import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface WebGuard
{
    
    boolean isValidRequest(HttpServletRequest request,
            HttpServletResponse response);
    
    void load(Properties properties);
    
}
