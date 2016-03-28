package com.codefarm.freemarker;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;

public class FreeMarkerConfiguration
        implements InitializingBean, ApplicationContextAware
{
    private static final Logger logger = LoggerFactory
            .getLogger(FreeMarkerConfiguration.class);
    
    private Resource configLocation;
    
    private String defaultEncoding;
    
    private String templateConfig;
    
    private Map<String, String> templates;
    
    private ApplicationContext context;
    
    @Override
    public void afterPropertiesSet() throws Exception
    {
        FreemarkerTool.setConfiguration(createConfiguration());
    }
    
    private Configuration createConfiguration()
            throws IOException, TemplateException
    {
        Configuration config = new Configuration();
        Properties props = new Properties();
        
        // Load config file if specified.
        if (this.configLocation != null)
        {
            if (logger.isInfoEnabled())
            {
                logger.info("Loading FreeMarker configuration from "
                        + this.configLocation);
            }
            PropertiesLoaderUtils.fillProperties(props, this.configLocation);
        }
        if (props.containsKey("auto_import"))
            props.put("auto_import", "");
        // FreeMarker will only accept known keys in its setSettings and
        // setAllSharedVariables methods.
        if (!props.isEmpty())
        {
            config.setSettings(props);
        }
        
        if (this.defaultEncoding != null)
        {
            config.setDefaultEncoding(this.defaultEncoding);
        }
        StringTemplateLoader loader = new StringTemplateLoader();
        loader.setTemplateConfig(templateConfig);
        loader.setTemplates(templates);
        config.setTemplateLoader(loader);
        return config;
    }
    
    public void setConfigLocation(Resource configLocation)
    {
        this.configLocation = configLocation;
    }
    
    public void setDefaultEncoding(String defaultEncoding)
    {
        this.defaultEncoding = defaultEncoding;
    }
    
    public void setTemplateConfig(String templateConfig)
    {
        this.templateConfig = templateConfig;
    }
    
    public void setTemplates(Map<String, String> templates)
    {
        this.templates = templates;
    }
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException
    {
        context = applicationContext;
    }
    
}
