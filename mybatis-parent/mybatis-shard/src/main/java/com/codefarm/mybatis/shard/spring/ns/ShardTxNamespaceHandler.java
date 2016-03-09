package com.codefarm.mybatis.shard.spring.ns;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;
import org.springframework.scheduling.config.AnnotationDrivenBeanDefinitionParser;
import org.springframework.transaction.config.JtaTransactionManagerBeanDefinitionParser;
import org.w3c.dom.Element;

public class ShardTxNamespaceHandler extends NamespaceHandlerSupport
{
    
    static final String TRANSACTION_MANAGER_ATTRIBUTE = "transaction-manager";
    
    static final String DEFAULT_TRANSACTION_MANAGER_BEAN_NAME = "transactionManager";
    
    public static String getTransactionManagerName(Element element)
    {
        return (element.hasAttribute(TRANSACTION_MANAGER_ATTRIBUTE) ? element.getAttribute(TRANSACTION_MANAGER_ATTRIBUTE)
                : DEFAULT_TRANSACTION_MANAGER_BEAN_NAME);
    }
    
    @Override
    public void init()
    {
        registerBeanDefinitionParser("advice",
                new ShardTxAdviceBeanDefinitionParser());
        registerBeanDefinitionParser("annotation-driven",
                new AnnotationDrivenBeanDefinitionParser());
        registerBeanDefinitionParser("jta-transaction-manager",
                new JtaTransactionManagerBeanDefinitionParser());
    }
    
}
