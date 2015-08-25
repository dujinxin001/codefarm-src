package com.sxj.web.actions;

import java.io.Serializable;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sxj.web.WebGuardException;

public interface IAction extends Serializable
{
    
    public void setName(String name);
    
    public String getName();
    
    public void setParameter(String name, String value);
    
    public String getParameter(String name);
    
    public Map<String, String> getParameterMap();
    
    public void execute(HttpServletRequest request,
            HttpServletResponse response, WebGuardException wge)
            throws WebGuardException;
    
}
