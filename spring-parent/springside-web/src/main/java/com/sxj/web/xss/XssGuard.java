package com.sxj.web.xss;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sxj.web.SingletonHolder;
import com.sxj.web.WebGuard;
import com.sxj.web.actions.IAction;

public class XssGuard implements WebGuard
{
    public final static String ACTION_PREFIX = "com.sxj.xss.action.";
    
    private Set<String> xssStrings = new HashSet<String>();
    
    private List<IAction> actions = null;
    
    private static final String XSS_PREFIX = "com.sxj.xss.STRING.";
    
    private Logger logger = LoggerFactory.getLogger(XssGuard.class);
    
    private final String[] DISABLE_SCRIPT = new String[] { "%3c", "<",
            "%3cscript", "<script", "alert" };
    
    public static XssGuard getInstance()
    {
        return SingletonHolder.XSS;
    }
    
    public XssGuard()
    {
        actions = new ArrayList<IAction>();
    }
    
    @Override
    public boolean isValidRequest(HttpServletRequest request,
            HttpServletResponse response)
    {
        Enumeration<String> en = request.getParameterNames();
        if (en != null)
        {
            while (en.hasMoreElements())
            {
                String key = en.nextElement();
                String value = request.getParameter(key);
                value = (value == null ? "" : value);
                //判断是否包含<script or javascript字符串,为xss的注入
                Iterator<String> iterator = xssStrings.iterator();
                while (iterator.hasNext())
                {
                    if (value.toLowerCase().indexOf(iterator.next()) != -1)
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    @Override
    public void load(Properties properties)
    {
        try
        {
            XssGuard xssGuard = SingletonHolder.XSS;
            for (Object obj : properties.keySet())
            {
                String key = (String) obj;
                
                if (key.startsWith(XSS_PREFIX))
                {
                    String directive = key.substring(XSS_PREFIX.length());
                    int index = directive.indexOf('.');
                    
                    /** page name/class **/
                    if (index < 0)
                    {
                        String str = properties.getProperty(key);
                        xssGuard.getXssStrings().add(str);
                    }
                }
                
            }
            if (xssGuard.getXssStrings().size() == 0)
                xssGuard.getXssStrings().addAll(Arrays.asList(DISABLE_SCRIPT));
            initActions(properties);
        }
        catch (Exception e)
        {
            getLogger().error("Error initialize XssGuard:", e);
        }
    }
    
    private void initActions(Properties properties)
            throws InstantiationException, IllegalAccessException,
            ClassNotFoundException, IOException
    {
        /** first pass: instantiate actions **/
        Map<String, IAction> actionsMap = new HashMap<String, IAction>();
        
        for (Object obj : properties.keySet())
        {
            String key = (String) obj;
            
            if (key.startsWith(ACTION_PREFIX))
            {
                String directive = key.substring(ACTION_PREFIX.length());
                int index = directive.indexOf('.');
                
                /** action name/class **/
                if (index < 0)
                {
                    String actionClass = properties.getProperty(key);
                    IAction action = (IAction) Class.forName(actionClass)
                            .newInstance();
                    
                    action.setName(directive);
                    actionsMap.put(action.getName(), action);
                    getInstance().getActions().add(action);
                }
            }
        }
        
        /** second pass: initialize action parameters **/
        for (Object obj : properties.keySet())
        {
            String key = (String) obj;
            
            if (key.startsWith(ACTION_PREFIX))
            {
                String directive = key.substring(ACTION_PREFIX.length());
                int index = directive.indexOf('.');
                
                /** action name/class **/
                if (index >= 0)
                {
                    String actionName = directive.substring(0, index);
                    IAction action = actionsMap.get(actionName);
                    
                    if (action == null)
                    {
                        throw new IOException(
                                String.format("action class %s has not yet been specified",
                                        actionName));
                    }
                    
                    String parameterName = directive.substring(index + 1);
                    String parameterValue = properties.getProperty(key);
                    
                    action.setParameter(parameterName, parameterValue);
                }
            }
        }
        
        /** ensure at least one action was defined **/
        if (getInstance().getActions().size() <= 0)
        {
            throw new IOException("failure to define at least one action");
        }
    }
    
    public Set<String> getXssStrings()
    {
        return xssStrings;
    }
    
    public void setXssStrings(Set<String> xssStrings)
    {
        this.xssStrings = xssStrings;
    }
    
    public List<IAction> getActions()
    {
        return actions;
    }
    
    public void setActions(List<IAction> actions)
    {
        this.actions = actions;
    }
    
    public Logger getLogger()
    {
        return logger;
    }
}
