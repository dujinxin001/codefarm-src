package com.sxj.freemarker;

import java.io.IOException;
import java.io.StringWriter;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class FreemarkerTool
{
    private static Configuration configuration;
    
    public static String processTemplateToString(Template template,
            Object model)
    {
        try
        {
            StringWriter result = new StringWriter();
            template.process(model, result);
            return result.toString();
        }
        catch (IOException ioe)
        {
            throw new FreemarkerException(ioe);
        }
        catch (TemplateException e)
        {
            throw new FreemarkerException(e);
        }
    }
    
    public static String processTemplateToString(String name, Object model)
    {
        try
        {
            if (configuration == null)
                configuration = new Configuration();
            Template template = configuration.getTemplate(name);
            return processTemplateToString(template, model);
        }
        catch (IOException ioe)
        {
            throw new FreemarkerException(ioe);
        }
    }
    
    protected static void setConfiguration(Configuration config)
    {
        configuration = config;
    }
}
