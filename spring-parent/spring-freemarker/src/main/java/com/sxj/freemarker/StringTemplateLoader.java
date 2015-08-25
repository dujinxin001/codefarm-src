package com.sxj.freemarker;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.sxj.spring.modules.util.ClassLoaderUtil;
import com.sxj.spring.modules.util.Collections3;
import com.sxj.spring.modules.util.StringUtils;

public class StringTemplateLoader extends freemarker.cache.StringTemplateLoader
{
    
    public void setTemplateConfig(String templateConfig)
    {
        if (StringUtils.isEmpty(templateConfig))
            return;
        try
        {
            InputStream resource = ClassLoaderUtil.getResource(templateConfig);
            Properties properties = new Properties();
            properties.load(resource);
            Enumeration<Object> keys = properties.keys();
            while (keys.hasMoreElements())
            {
                String nextElement = (String) keys.nextElement();
                putTemplate(nextElement, properties.getProperty(nextElement));
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
    
    public void setTemplates(Map<String, String> templates)
    {
        if (Collections3.isEmpty(templates))
            return;
        Set<String> keySet = templates.keySet();
        Iterator<String> iterator = keySet.iterator();
        while (iterator.hasNext())
        {
            String next = iterator.next();
            putTemplate(next, templates.get(next));
        }
    }
    
}
