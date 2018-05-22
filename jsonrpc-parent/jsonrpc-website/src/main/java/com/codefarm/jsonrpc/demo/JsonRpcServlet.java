package com.codefarm.jsonrpc.demo;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import com.codefarm.jsonrpc.server.spring.AutoJsonRpcServiceExporter;

public class JsonRpcServlet extends DispatcherServlet
{
    
    @Override
    protected void applyInitializers(ConfigurableApplicationContext wac)
    {
        BeanDefinitionBuilder beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(AutoJsonRpcServiceExporter.class);
        
        GenericApplicationContext context = new GenericApplicationContext(wac);
        context.registerBeanDefinition("", beanDefinition.getBeanDefinition());
        // TODO Auto-generated method stub
        super.applyInitializers(wac);
    }
    
}
