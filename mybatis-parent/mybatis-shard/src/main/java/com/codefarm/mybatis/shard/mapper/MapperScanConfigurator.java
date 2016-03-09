package com.codefarm.mybatis.shard.mapper;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.io.Resource;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.util.StringUtils;

import com.codefarm.mybatis.shard.MybatisConfiguration;
import com.codefarm.mybatis.shard.datasource.DataSourceFactory;

public class MapperScanConfigurator implements
        BeanDefinitionRegistryPostProcessor, ApplicationContextAware
{
    
    private String basePackage;
    
    private static String typeAliasesPackage;
    
    private Resource configLocation;
    
    private static Resource[] mapperLocations;
    
    private Class<? extends Annotation> annotationClass;
    
    private Class<?> markerInterface;
    
    private ApplicationContext applicationContext;
    
    private static Set<String> mapperInterfaces = new HashSet<String>();
    
    private Scanner scanner;
    
    @Override
    public void postProcessBeanDefinitionRegistry(
            BeanDefinitionRegistry registry) throws BeansException
    {
        // 初始化所有 mapper interfaces
        scanner = new Scanner(registry);
        scanner.setResourceLoader(this.applicationContext);
        scanner.scan(StringUtils.tokenizeToStringArray(this.basePackage,
                ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS));
        //初始化所有数据源、sessionFactory
        MybatisConfiguration.setApplicationContext(applicationContext);
        MybatisConfiguration.setConfigLocation(configLocation);
        DataSourceFactory.setContext(applicationContext);
    }
    
    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException
    {
        this.applicationContext = applicationContext;
    }
    
    public void setBasePackage(String basePackage)
    {
        this.basePackage = basePackage;
    }
    
    //    private void findEntityClassNames() throws IOException
    //    {
    //        SimpleMetadataReaderFactory metadataReaderFactory = new SimpleMetadataReaderFactory(
    //                applicationContext);
    //        String fieldValue = basePackage == null ? "" : basePackage;
    //        String[] split = fieldValue.split(",");
    //        for (String value : split)
    //        {
    //            Resource[] resources = applicationContext.getResources("classpath:"
    //                    + StringUtils.replace(value, ".", "/") + "/**/*.class");
    //            for (Resource resource : resources)
    //            {
    //                if (resource.isReadable())
    //                {
    //                    MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
    //                    ClassMetadata classMetadata = metadataReader.getClassMetadata();
    //                    mapperInterfaces.add(classMetadata.getClassName());
    //                    //                AnnotationMetadata annotationMetadata = metadataReader.getAnnotationMetadata();
    //                    //                String entityAnnotation = Entity.class.getName();
    //                    //                if (annotationMetadata.isAnnotated(entityAnnotation))
    //                    //                {
    //                    //                    classNames.add(classMetadata.getClassName());
    //                    //                }
    //                }
    //            }
    //        }
    //        
    //    }
    
    private final class Scanner extends ClassPathBeanDefinitionScanner
    {
        
        public Scanner(BeanDefinitionRegistry registry)
        {
            super(registry);
        }
        
        protected void registerDefaultFilters()
        {
            boolean acceptAllInterfaces = true;
            
            // if specified, use the given annotation and / or marker interface
            if (MapperScanConfigurator.this.annotationClass != null)
            {
                addIncludeFilter(new AnnotationTypeFilter(
                        MapperScanConfigurator.this.annotationClass));
                acceptAllInterfaces = false;
            }
            
            // override AssignableTypeFilter to ignore matches on the actual marker interface
            if (MapperScanConfigurator.this.markerInterface != null)
            {
                addIncludeFilter(new AssignableTypeFilter(
                        MapperScanConfigurator.this.markerInterface)
                {
                    @Override
                    protected boolean matchClassName(String className)
                    {
                        return false;
                    }
                });
                acceptAllInterfaces = false;
            }
            
            if (acceptAllInterfaces)
            {
                // default include filter that accepts all classes
                addIncludeFilter(new TypeFilter()
                {
                    public boolean match(MetadataReader metadataReader,
                            MetadataReaderFactory metadataReaderFactory)
                            throws IOException
                    {
                        return true;
                    }
                });
            }
            
            // exclude package-info.java
            addExcludeFilter(new TypeFilter()
            {
                public boolean match(MetadataReader metadataReader,
                        MetadataReaderFactory metadataReaderFactory)
                        throws IOException
                {
                    String className = metadataReader.getClassMetadata()
                            .getClassName();
                    return className.endsWith("package-info");
                }
            });
        }
        
        protected Set<BeanDefinitionHolder> doScan(String... basePackages)
        {
            Set<BeanDefinitionHolder> beanDefinitions = super.doScan(basePackages);
            
            if (beanDefinitions.isEmpty())
            {
                logger.warn("No MyBatis mapper was found in '"
                        + MapperScanConfigurator.this.basePackage
                        + "' package. Please check your configuration.");
            }
            else
            {
                for (BeanDefinitionHolder holder : beanDefinitions)
                {
                    GenericBeanDefinition definition = (GenericBeanDefinition) holder.getBeanDefinition();
                    mapperInterfaces.add(definition.getBeanClassName());
                    definition.getPropertyValues().add("mapperInterface",
                            definition.getBeanClassName());
                    definition.setBeanClass(ShardMapperFactoryBean.class);
                }
            }
            
            return beanDefinitions;
        }
        
        protected boolean isCandidateComponent(
                AnnotatedBeanDefinition beanDefinition)
        {
            return (beanDefinition.getMetadata().isInterface() && beanDefinition.getMetadata()
                    .isIndependent());
        }
        
        protected boolean checkCandidate(String beanName,
                BeanDefinition beanDefinition) throws IllegalStateException
        {
            if (super.checkCandidate(beanName, beanDefinition))
            {
                return true;
            }
            else
            {
                logger.warn("Skipping MapperFactoryBean with name '" + beanName
                        + "' and '" + beanDefinition.getBeanClassName()
                        + "' mapperInterface"
                        + ". Bean already defined with the same name!");
                return false;
            }
        }
    }
    
    @Override
    public void postProcessBeanFactory(
            ConfigurableListableBeanFactory beanFactory) throws BeansException
    {
    }
    
    public static Set<String> getMapperInterfaces()
    {
        return mapperInterfaces;
    }
    
    public void setConfigLocation(Resource configLocation)
    {
        this.configLocation = configLocation;
    }
    
    public static Resource[] getMapperLocations()
    {
        return mapperLocations;
    }
    
    public void setMapperLocations(Resource[] mapperLocations)
    {
        MapperScanConfigurator.mapperLocations = mapperLocations;
    }
    
    public static String getTypeAliasesPackage()
    {
        return typeAliasesPackage;
    }
    
    public void setTypeAliasesPackage(String typeAliasesPackage)
    {
        MapperScanConfigurator.typeAliasesPackage = typeAliasesPackage;
    }
    
}
