package com.sxj.mybatis.orm;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.session.AutoMappingBehavior;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.SimpleMetadataReaderFactory;

import com.sxj.mybatis.orm.annotations.Entity;
import com.sxj.mybatis.orm.builder.GenericStatementBuilder;
import com.sxj.spring.modules.util.Reflections;

/**
 * Class description goes here.
 */
public class ActiveSQLSessionFactoryBean extends SqlSessionFactoryBean
        implements ApplicationContextAware
{
    
    private Configuration configuration;
    
    //    private ResourceLoader resourceLoader;
    
    private ApplicationContext applicationContext;
    
    private Map<String, MappedStatement> refreshed = new HashMap<String, MappedStatement>();
    
    @Override
    public void afterPropertiesSet() throws Exception
    {
        super.afterPropertiesSet();
        SqlSessionFactory sqlSessionFactory = super.getObject();
        configuration = sqlSessionFactory.getConfiguration();
        configuration.setAutoMappingBehavior(AutoMappingBehavior.NONE);
        refreshMappedStatements();
        for (String clazzName : findEntityClassNames())
        {
            GenericStatementBuilder builder = new GenericStatementBuilder(
                    configuration, Class.forName(clazzName));
            builder.build();
        }
    }
    
    private void refreshMappedStatements()
    {
        Map<String, MappedStatement> mappedStatements = (Map<String, MappedStatement>) Reflections.getFieldValue(configuration,
                "mappedStatements");
        Collection<MappedStatement> values = mappedStatements.values();
        for (Object value : values)
        {
            if (value instanceof MappedStatement)
            {
                MappedStatement mappedStatement = (MappedStatement) value;
                Class<?> entityClass = mappedStatement.getParameterMap()
                        .getType();
                if (mappedStatement instanceof MappedStatement
                        && mappedStatement.getSqlCommandType() == SqlCommandType.INSERT
                        && entityClass.isAnnotationPresent(Entity.class))
                {
                    refreshed.put(mappedStatement.getId(), mappedStatement);
                }
            }
        }
        Set<String> refreshedKeys = refreshed.keySet();
        for (String key : refreshedKeys)
        {
            MappedStatement mappedStatement = mappedStatements.get(key);
            mappedStatements.remove(key);
            new GenericStatementBuilder(configuration,
                    mappedStatement.getParameterMap().getType()).refresh(mappedStatement);
            ;
        }
    }
    
    @Override
    public SqlSessionFactory getObject() throws Exception
    {
        return super.getObject();
    }
    
    private Set<String> findEntityClassNames() throws IOException
    {
        Set<String> classNames = new HashSet<String>();
        SimpleMetadataReaderFactory metadataReaderFactory = new SimpleMetadataReaderFactory(
                applicationContext);
        String fieldValue = (String) Reflections.getFieldValue(this,
                "typeAliasesPackage");
        fieldValue = fieldValue == null ? "" : fieldValue;
        String[] split = fieldValue.split(",");
        for (String value : split)
        {
            Resource[] resources = applicationContext.getResources("classpath*:"
                    + StringUtils.replaceChars(value, '.', '/') + "/**/*.class");
            for (Resource resource : resources)
            {
                if (resource.isReadable())
                {
                    MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
                    ClassMetadata classMetadata = metadataReader.getClassMetadata();
                    AnnotationMetadata annotationMetadata = metadataReader.getAnnotationMetadata();
                    String entityAnnotation = Entity.class.getName();
                    if (annotationMetadata.isAnnotated(entityAnnotation))
                    {
                        classNames.add(classMetadata.getClassName());
                    }
                }
            }
        }
        
        return classNames;
    }
    
    //    private URL[] findClassPath() throws IOException
    //    {
    //        ResourcePatternResolver resourcePatternResolver = ResourcePatternUtils.getResourcePatternResolver(resourceLoader);
    //        String fieldValue = (String) Reflections.getFieldValue(this,
    //                "typeAliasesPackage");
    //        Resource[] resources = resourcePatternResolver.getResources("classpath:"
    //                + StringUtils.replaceChars(fieldValue, '.', '/')
    //                + "/**/*.class");
    //        URL[] classPaths = new URL[resources.length];
    //        for (int i = 0; i < resources.length; i++)
    //            classPaths[i] = resources[i].getURL();
    //        return classPaths;
    //    }
    //    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException
    {
        this.applicationContext = applicationContext;
    }
    
}
