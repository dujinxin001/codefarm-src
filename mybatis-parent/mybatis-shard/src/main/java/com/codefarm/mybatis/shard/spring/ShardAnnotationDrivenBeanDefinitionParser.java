package com.codefarm.mybatis.shard.spring;

import org.springframework.aop.config.AopNamespaceUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.parsing.CompositeComponentDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.transaction.config.TransactionManagementConfigUtils;
import org.springframework.transaction.interceptor.BeanFactoryTransactionAttributeSourceAdvisor;
import org.w3c.dom.Element;

import com.codefarm.mybatis.shard.spring.aop.ShardTransactionInterceptor;
import com.codefarm.mybatis.shard.spring.ns.ShardTxNamespaceHandler;

public class ShardAnnotationDrivenBeanDefinitionParser implements
        BeanDefinitionParser
{
    /**
     * The bean name of the internally managed transaction advisor (mode="proxy").
     * @deprecated as of Spring 3.1 in favor of
     * {@link TransactionManagementConfigUtils#TRANSACTION_ADVISOR_BEAN_NAME}
     */
    @Deprecated
    public static final String TRANSACTION_ADVISOR_BEAN_NAME = TransactionManagementConfigUtils.TRANSACTION_ADVISOR_BEAN_NAME;
    
    /**
     * The bean name of the internally managed transaction aspect (mode="aspectj").
     * @deprecated as of Spring 3.1 in favor of
     * {@link TransactionManagementConfigUtils#TRANSACTION_ASPECT_BEAN_NAME}
     */
    @Deprecated
    public static final String TRANSACTION_ASPECT_BEAN_NAME = TransactionManagementConfigUtils.TRANSACTION_ASPECT_BEAN_NAME;
    
    /**
     * Parses the {@code <tx:annotation-driven/>} tag. Will
     * {@link AopNamespaceUtils#registerAutoProxyCreatorIfNecessary register an AutoProxyCreator}
     * with the container as necessary.
     */
    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext)
    {
        String mode = element.getAttribute("mode");
        if ("aspectj".equals(mode))
        {
            // mode="aspectj"
            registerTransactionAspect(element, parserContext);
        }
        else
        {
            // mode="proxy"
            AopAutoProxyConfigurer.configureAutoProxyCreator(element,
                    parserContext);
        }
        return null;
    }
    
    private void registerTransactionAspect(Element element,
            ParserContext parserContext)
    {
        String txAspectBeanName = TransactionManagementConfigUtils.TRANSACTION_ASPECT_BEAN_NAME;
        String txAspectClassName = TransactionManagementConfigUtils.TRANSACTION_ASPECT_CLASS_NAME;
        if (!parserContext.getRegistry()
                .containsBeanDefinition(txAspectBeanName))
        {
            RootBeanDefinition def = new RootBeanDefinition();
            def.setBeanClassName(txAspectClassName);
            def.setFactoryMethodName("aspectOf");
            registerTransactionManager(element, def);
            parserContext.registerBeanComponent(new BeanComponentDefinition(
                    def, txAspectBeanName));
        }
    }
    
    private static void registerTransactionManager(Element element,
            BeanDefinition def)
    {
        def.getPropertyValues().add("transactionManagerBeanName",
                ShardTxNamespaceHandler.getTransactionManagerName(element));
    }
    
    /**
     * Inner class to just introduce an AOP framework dependency when actually in proxy mode.
     */
    private static class AopAutoProxyConfigurer
    {
        
        public static void configureAutoProxyCreator(Element element,
                ParserContext parserContext)
        {
            AopNamespaceUtils.registerAutoProxyCreatorIfNecessary(parserContext,
                    element);
            
            String txAdvisorBeanName = TransactionManagementConfigUtils.TRANSACTION_ADVISOR_BEAN_NAME;
            if (!parserContext.getRegistry()
                    .containsBeanDefinition(txAdvisorBeanName))
            {
                Object eleSource = parserContext.extractSource(element);
                
                // Create the TransactionAttributeSource definition.
                RootBeanDefinition sourceDef = new RootBeanDefinition(
                        "org.springframework.transaction.annotation.AnnotationTransactionAttributeSource");
                sourceDef.setSource(eleSource);
                sourceDef.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
                String sourceName = parserContext.getReaderContext()
                        .registerWithGeneratedName(sourceDef);
                
                // Create the TransactionInterceptor definition.
                RootBeanDefinition interceptorDef = new RootBeanDefinition(
                        ShardTransactionInterceptor.class);
                interceptorDef.setSource(eleSource);
                interceptorDef.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
                registerTransactionManager(element, interceptorDef);
                interceptorDef.getPropertyValues()
                        .add("transactionAttributeSource",
                                new RuntimeBeanReference(sourceName));
                String interceptorName = parserContext.getReaderContext()
                        .registerWithGeneratedName(interceptorDef);
                
                // Create the TransactionAttributeSourceAdvisor definition.
                RootBeanDefinition advisorDef = new RootBeanDefinition(
                        BeanFactoryTransactionAttributeSourceAdvisor.class);
                advisorDef.setSource(eleSource);
                advisorDef.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
                advisorDef.getPropertyValues()
                        .add("transactionAttributeSource",
                                new RuntimeBeanReference(sourceName));
                advisorDef.getPropertyValues().add("adviceBeanName",
                        interceptorName);
                if (element.hasAttribute("order"))
                {
                    advisorDef.getPropertyValues().add("order",
                            element.getAttribute("order"));
                }
                parserContext.getRegistry()
                        .registerBeanDefinition(txAdvisorBeanName, advisorDef);
                
                CompositeComponentDefinition compositeDef = new CompositeComponentDefinition(
                        element.getTagName(), eleSource);
                compositeDef.addNestedComponent(new BeanComponentDefinition(
                        sourceDef, sourceName));
                compositeDef.addNestedComponent(new BeanComponentDefinition(
                        interceptorDef, interceptorName));
                compositeDef.addNestedComponent(new BeanComponentDefinition(
                        advisorDef, txAdvisorBeanName));
                parserContext.registerComponent(compositeDef);
            }
        }
    }
}
