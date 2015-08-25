package com.sxj.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 
 */
public interface Protocol
{
    
    boolean wasViolated(HttpServletRequest request, HttpServletResponse response);
    
    String rejectionCause();
    
}
