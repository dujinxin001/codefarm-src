/*
 * @(#)GenericStatementBuilder.java 2013年12月23日 下午23:33:33
 *
 * Copyright (c) 2011-2013 Makersoft.org all rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 */
package com.codefarm.mybatis.orm.builder;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.builder.BaseBuilder;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.executor.keygen.NoKeyGenerator;
import org.apache.ibatis.executor.keygen.SelectKeyGenerator;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMap;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.mapping.StatementType;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.scripting.xmltags.DynamicSqlSource;
import org.apache.ibatis.session.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.FieldCallback;
import org.springframework.util.ReflectionUtils.FieldFilter;

import com.codefarm.mybatis.orm.ConfigurationProperties;
import com.codefarm.mybatis.orm.ResultMapBuilder;
import com.codefarm.mybatis.orm.annotations.Column;
import com.codefarm.mybatis.orm.annotations.Criterias;
import com.codefarm.mybatis.orm.annotations.Delete;
import com.codefarm.mybatis.orm.annotations.Entity;
import com.codefarm.mybatis.orm.annotations.GeneratedValue;
import com.codefarm.mybatis.orm.annotations.GenerationType;
import com.codefarm.mybatis.orm.annotations.Id;
import com.codefarm.mybatis.orm.annotations.Insert;
import com.codefarm.mybatis.orm.annotations.Select;
import com.codefarm.mybatis.orm.annotations.Sn;
import com.codefarm.mybatis.orm.annotations.Transient;
import com.codefarm.mybatis.orm.annotations.Update;
import com.codefarm.mybatis.orm.annotations.Version;
import com.codefarm.mybatis.orm.keygen.Jdbc4KeyGenerator;
import com.codefarm.mybatis.orm.keygen.LongSequenceKeyGenerator;
import com.codefarm.mybatis.orm.keygen.ShardJdbc4KeyGenerator;
import com.codefarm.mybatis.orm.keygen.ShardKeyGenerator;
import com.codefarm.mybatis.orm.keygen.ShardSnGenerator;
import com.codefarm.mybatis.orm.keygen.ShardUuidKeyGenerator;
import com.codefarm.mybatis.orm.keygen.SnGenerator;
import com.codefarm.mybatis.orm.keygen.UuidKeyGenerator;
import com.codefarm.spring.modules.util.AnnotationUtils;
import com.codefarm.spring.modules.util.CaseFormatUtils;
import com.codefarm.spring.modules.util.ReflectUtils;

/**
 * Class description goes here.
 * 
 */
public class GenericStatementBuilder extends BaseBuilder
{
    private static final Logger logger = LoggerFactory
            .getLogger(GenericStatementBuilder.class);
    
    private MapperBuilderAssistant assistant;
    
    private static Map<String, ShardKeyGenerator> shardedKeyGenerators = new HashMap<String, ShardKeyGenerator>();
    
    private static Map<String, ShardSnGenerator> shardSnGenerators = new HashMap<String, ShardSnGenerator>();
    
    private static Map<String, SnGenerator> snGenerators = new HashMap<String, SnGenerator>();
    
    /**
     * 是否包含SN属性
     */
    private boolean containSn = false;
    
    /**
     * 实体类
     */
    private Class<?> entityClass;
    
    private String databaseId;
    
    private LanguageDriver lang;
    
    /**
     * 主键属性
     */
    private Field idField;
    
    /**
     * 乐观锁属性
     */
    private Field versionField;
    
    private List<Field> columnFields = new ArrayList<Field>();
    
    /**
     * Mapper接口
     */
    private Class<?> mapperType;
    
    private Entity entity;
    
    /**
     * Mybatis注册名称空间
     */
    private String namespace;
    
    private boolean sharded = false;
    
    public GenericStatementBuilder(Configuration configuration,
            final Class<?> entityClass)
    {
        super(configuration);
        this.entityClass = entityClass;
        readConfiguration(configuration);
        initAssistant(configuration, entityClass);
        parseEntity(entityClass);
    }
    
    /**
     * @param entityClass
     * 解析Entity
     */
    private void parseEntity(final Class<?> entityClass)
    {
        
        //主键属性
        idField = AnnotationUtils.findDeclaredFieldWithAnnoation(Id.class,
                entityClass);
        //乐观锁属性
        versionField = AnnotationUtils
                .findDeclaredFieldWithAnnoation(Version.class, entityClass);
        //其他表属性
        ReflectionUtils.doWithFields(entityClass, new FieldCallback()
        {
            
            @Override
            public void doWith(Field field)
                    throws IllegalArgumentException, IllegalAccessException
            {
                if (field.isAnnotationPresent(Column.class)
                        || field.isAnnotationPresent(Id.class))
                    columnFields.add(field);
                if (field.isAnnotationPresent(Sn.class))
                    containSn = true;
                
            }
        }, new FieldFilter()
        {
            
            @Override
            public boolean matches(Field field)
            {
                if (Modifier.isStatic(field.getModifiers())
                        || Modifier.isFinal(field.getModifiers()))
                {
                    return false;
                }
                
                for (Annotation annotation : field.getAnnotations())
                {
                    if (Transient.class.isAssignableFrom(annotation.getClass()))
                    {
                        
                        return false;
                    }
                }
                
                return true;
            }
        });
    }
    
    private void readConfiguration(Configuration configuration)
    {
        //是否分库
        sharded = ConfigurationProperties.isSharded(configuration);
        databaseId = super.getConfiguration().getDatabaseId();
        lang = super.getConfiguration().getDefaultScriptingLanuageInstance();
    }
    
    private void initAssistant(Configuration configuration,
            final Class<?> entityClass)
    {
        String resource = entityClass.getName().replace('.', '/')
                + ".java (best guess)";
        assistant = new MapperBuilderAssistant(configuration, resource);
        entity = entityClass.getAnnotation(Entity.class);
        mapperType = entity.mapper();
        
        if (!mapperType.isAssignableFrom(Void.class))
        {
            namespace = mapperType.getName();
        }
        else
        {
            namespace = entityClass.getName();
        }
        assistant.setCurrentNamespace(namespace);
        Collection<String> cacheNames = configuration.getCacheNames();
        for (String name : cacheNames)
            if (namespace.equals(name))
            {
                assistant.useCacheRef(name);
                break;
            }
    }
    
    public void build()
    {
        //----------inserts------------//
        buildInsertStatements();
        //----------deletes------------//
        buildDeleteStatements();
        //----------updates------------//
        buildUpdateStatements();
        //----------selects------------//
        buildSelectStatements();
        
    }
    
    private void buildUpdateStatements()
    {
        String updateStatementId;
        List<Method> updateMethods = ReflectUtils
                .findMethodsAnnotatedWith(mapperType, Update.class);
        for (Method method : updateMethods)
        {
            updateStatementId = method.getName();
            if (!hasStatement(updateStatementId))
            {
                buildUpdate(namespace + "." + updateStatementId, method);
            }
        }
    }
    
    private void buildDeleteStatements()
    {
        String deleteStatementId;
        List<Method> deleteMethods = ReflectUtils
                .findMethodsAnnotatedWith(mapperType, Delete.class);
        for (Method method : deleteMethods)
        {
            deleteStatementId = method.getName();
            if (!hasStatement(deleteStatementId))
            {
                buildDelete(namespace + "." + deleteStatementId, method);
            }
        }
    }
    
    private void buildSelectStatements()
    {
        String selectStatementId;
        List<Method> selectMethods = ReflectUtils
                .findMethodsAnnotatedWith(mapperType, Select.class);
        for (Method method : selectMethods)
        {
            selectStatementId = method.getName();
            if (!hasStatement(selectStatementId))
            {
                buildSelect(namespace + "." + selectStatementId, method);
            }
        }
    }
    
    private boolean hasStatement(String selectStatementId)
    {
        return super.getConfiguration()
                .hasStatement(namespace + "." + selectStatementId);
    }
    
    private void buildInsertStatements()
    {
        String insertStatementId;
        List<Method> insertMethods = ReflectUtils
                .findMethodsAnnotatedWith(mapperType, Insert.class);
        for (Method method : insertMethods)
        {
            insertStatementId = method.getName();
            if (!hasStatement(insertStatementId))
            {
                buildInsert(namespace + "." + insertStatementId, method);
            }
        }
    }
    
    public void refresh(MappedStatement mappedStatement)
    {
        Integer timeout = null;
        Class<?> parameterType = entityClass;
        
        ///~~~~~~~~~~
        boolean flushCache = true;
        boolean useCache = false;
        boolean resultOrdered = false;
        KeyGenerator keyGenerator = new NoKeyGenerator();
        String keyProperty = null;
        String keyColumn = null;
        
        Id id = AnnotationUtils.findDeclaredAnnotation(Id.class, entityClass);
        GeneratedValue generatedValue = AnnotationUtils
                .findDeclaredAnnotation(GeneratedValue.class, entityClass);
        if (id != null)
        {
            String keyStatementId = entityClass.getName() + ".insert"
                    + SelectKeyGenerator.SELECT_KEY_SUFFIX;
            if (!sharded)
            {
                
                if (containSn)
                    snGenerators.put(mappedStatement.getId(),
                            new SnGenerator());
                if (configuration.hasKeyGenerator(keyStatementId))
                {
                    keyGenerator = configuration
                            .getKeyGenerator(keyStatementId);
                }
                else if (generatedValue != null)
                {
                    if (generatedValue.strategy() == GenerationType.UUID)
                    {
                        keyGenerator = new UuidKeyGenerator(
                                generatedValue.length());
                    }
                }
                else
                {
                    keyGenerator = id.generatedKeys() ? new Jdbc4KeyGenerator()
                            : new NoKeyGenerator();
                }
            }
            else
            {
                if (containSn)
                    shardSnGenerators.put(mappedStatement.getId(),
                            new ShardSnGenerator());
                if (generatedValue != null)
                {
                    if (generatedValue.strategy() == GenerationType.UUID)
                    {
                        shardedKeyGenerators.put(mappedStatement.getId(),
                                new ShardUuidKeyGenerator(
                                        generatedValue.length()));
                    }
                    else if (generatedValue.strategy() == GenerationType.TABLE
                            || generatedValue.strategy() == GenerationType.AUTO)
                    {
                        shardedKeyGenerators.put(mappedStatement.getId(),
                                new ShardJdbc4KeyGenerator());
                    }
                }
                //                shardedKeyGenerators.put(statementId, new shardeduu)
            }
            keyProperty = idField.getName();
            keyColumn = StringUtils.isBlank(id.column())
                    ? CaseFormatUtils.camelToUnderScore(idField.getName())
                    : id.column();
        }
        
        SqlSource sqlSource = mappedStatement.getSqlSource();
        String parameterMap = null;
        parameterMap = findParameterMap();
        assistant.addMappedStatement(mappedStatement.getId(),
                sqlSource,
                StatementType.PREPARED,
                SqlCommandType.INSERT,
                null,
                timeout,
                parameterMap,
                parameterType,
                null,
                null,
                null,
                flushCache,
                useCache,
                resultOrdered,
                keyGenerator,
                keyProperty,
                keyColumn,
                databaseId,
                lang);
    }
    
    private void buildInsert(String statementId, Method method)
    {
        
        Integer timeout = null;
        Class<?> parameterType = entityClass;
        
        ///~~~~~~~~~~
        boolean flushCache = true;
        boolean useCache = false;
        boolean resultOrdered = false;
        KeyGenerator keyGenerator = new NoKeyGenerator();
        String keyProperty = idField.getName();
        String keyColumn = null;
        
        Id id = AnnotationUtils.findDeclaredAnnotation(Id.class, entityClass);
        
        if (id != null && id.generatedKeys())
        {
            String keyStatementId = entityClass.getName() + ".insert"
                    + SelectKeyGenerator.SELECT_KEY_SUFFIX;
            keyGenerator = buildKeyGenerator(statementId, keyStatementId);
            keyColumn = StringUtils.isBlank(id.column())
                    ? CaseFormatUtils.camelToUnderScore(idField.getName())
                    : id.column();
            GeneratedValue annotation = idField
                    .getAnnotation(GeneratedValue.class);
            if (annotation != null && !annotation.generator()
                    .isAssignableFrom(NoKeyGenerator.class))
                try
                {
                    keyGenerator = annotation.generator()
                            .getDeclaredConstructor(String.class)
                            .newInstance(annotation.sequence());
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
        }
        
        SqlSource sqlSource = new DynamicSqlSource(configuration,
                new InsertSqlNodeBuilder().build(configuration,
                        createMappedMethod(method)));
        String parameterMap = null;
        parameterMap = findParameterMap();
        logger.info("绑定Map {}", statementId);
        assistant.addMappedStatement(statementId,
                sqlSource,
                StatementType.PREPARED,
                SqlCommandType.INSERT,
                null,
                timeout,
                parameterMap,
                parameterType,
                null,
                null,
                null,
                flushCache,
                useCache,
                resultOrdered,
                keyGenerator,
                keyProperty,
                keyColumn,
                databaseId,
                lang);
    }
    
    private String findParameterMap()
    {
        Iterator<String> parameterMapNames = configuration
                .getParameterMapNames().iterator();
        while (parameterMapNames.hasNext())
        {
            String name = parameterMapNames.next();
            ParameterMap temp = configuration.getParameterMap(name);
            if (temp.getType().equals(entityClass))
            {
                return temp.getId();
            }
        }
        return null;
    }
    
    private void buildShardedKeyGenerator(String statementId)
    {
        GeneratedValue generatedValue = AnnotationUtils
                .findDeclaredAnnotation(GeneratedValue.class, entityClass);
        if (containSn)
            shardSnGenerators.put(statementId, new ShardSnGenerator());
        if (generatedValue != null)
        {
            if (generatedValue.strategy() == GenerationType.UUID)
            {
                shardedKeyGenerators.put(statementId,
                        new ShardUuidKeyGenerator(generatedValue.length()));
            }
            else if (generatedValue.strategy() == GenerationType.TABLE
                    || generatedValue.strategy() == GenerationType.AUTO)
            {
                shardedKeyGenerators.put(statementId,
                        new ShardJdbc4KeyGenerator());
            }
        }
    }
    
    private KeyGenerator buildKeyGenerator(String statementId,
            String keyStatementId)
    {
        if (sharded)
        {
            buildShardedKeyGenerator(keyStatementId);
            return null;
        }
        
        GeneratedValue generatedValue = AnnotationUtils
                .findDeclaredAnnotation(GeneratedValue.class, entityClass);
        if (containSn)
            snGenerators.put(statementId, new SnGenerator());
        if (configuration.hasKeyGenerator(keyStatementId))
        {
            return configuration.getKeyGenerator(keyStatementId);
        }
        else if (generatedValue != null)
        {
            if (generatedValue.strategy() == GenerationType.UUID)
            {
                return new UuidKeyGenerator(generatedValue.length());
                
            }
            else if (generatedValue.strategy() == GenerationType.SEQUENCE)
            {
                return new LongSequenceKeyGenerator(generatedValue.sequence());
            }
        }
        return new NoKeyGenerator();
    }
    
    private void buildDelete(String statementId, Method method)
    {
        Integer timeout = null;
        Class<?> parameterType = entityClass;
        
        boolean flushCache = true;
        boolean useCache = false;
        boolean resultOrdered = false;
        KeyGenerator keyGenerator = new NoKeyGenerator();
        SqlSource sqlSource = new DynamicSqlSource(configuration,
                new DeleteSqlNodeBuilder().build(configuration,
                        createMappedMethod(method)));
        
        assistant.addMappedStatement(statementId,
                sqlSource,
                StatementType.PREPARED,
                SqlCommandType.DELETE,
                null,
                timeout,
                null,
                parameterType,
                null,
                null,
                null,
                flushCache,
                useCache,
                resultOrdered,
                keyGenerator,
                null,
                null,
                databaseId,
                lang);
    }
    
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //update
    private void buildUpdate(String statementId, Method method)
    {
        Integer timeout = null;
        Class<?> parameterType = entityClass;
        
        //~~~~~~~~~~~~~
        boolean flushCache = true;
        boolean useCache = false;
        boolean resultOrdered = false;
        KeyGenerator keyGenerator = new NoKeyGenerator();
        
        SqlSource sqlSource = new DynamicSqlSource(configuration,
                new UpdateSqlNodeBuilder().build(configuration,
                        createMappedMethod(method)));
        String parameterMap = null;
        parameterMap = findParameterMap();
        assistant.addMappedStatement(statementId,
                sqlSource,
                StatementType.PREPARED,
                SqlCommandType.UPDATE,
                null,
                timeout,
                parameterMap,
                parameterType,
                null,
                null,
                null,
                flushCache,
                useCache,
                resultOrdered,
                keyGenerator,
                null,
                null,
                databaseId,
                lang);
    }
    
    private Class<?> findParameterType(Method method)
    {
        Class<?>[] parameterTypes = method.getParameterTypes();
        for (Class<?> clazz : parameterTypes)
        {
            if (clazz.isAnnotationPresent(Criterias.class))
                return clazz;
        }
        return null;
    }
    
    //~~~~~~~~~~~~~~~~~
    //get
    private void buildSelect(String statementId, Method method)
    {
        Integer fetchSize = null;
        Integer timeout = entity.timeout() == -1 ? null : entity.timeout();
        Class<?> resultType = entityClass;
        Class<?> parameterType = findParameterType(method);
        if (parameterType == null)
            parameterType = idField.getType();
        boolean flushCache = entity.flushCache();
        boolean useCache = entity.useCache();
        boolean resultOrdered = false;
        KeyGenerator keyGenerator = new NoKeyGenerator();
        
        //        List<SqlNode> contents = new ArrayList<SqlNode>();
        //        contents.add(this.getSelectSql(method));
        SqlSource sqlSource = new DynamicSqlSource(configuration,
                new SelectSqlNodeBuilder().build(configuration,
                        createMappedMethod(method)));
        
        String resultMap = findResultMap();
        logger.info("绑定Map {}", statementId);
        assistant.addMappedStatement(statementId,
                sqlSource,
                StatementType.PREPARED,
                SqlCommandType.SELECT,
                fetchSize,
                timeout,
                null,
                parameterType,
                resultMap,
                resultType,
                null,
                flushCache,
                useCache,
                resultOrdered,
                keyGenerator,
                null,
                null,
                databaseId,
                lang);
    }
    
    private MappedMethod createMappedMethod(Method method)
    {
        MappedMethod mappedMethod = new MappedMethod(entityClass, columnFields,
                idField);
        mappedMethod.setMethod(method);
        mappedMethod.setVersionField(versionField);
        return mappedMethod;
    }
    
    private String findResultMap()
    {
        String resultMap = null;
        Iterator<String> resultMapNames = configuration.getResultMapNames()
                .iterator();
        try
        {
            while (resultMapNames.hasNext())
            {
                String name = resultMapNames.next();
                ResultMap temp = configuration
                        .getResultMap(namespace + "." + name);
                if (temp.getType().equals(entityClass))
                {
                    resultMap = temp.getId();
                    break;
                }
            }
        }
        catch (Exception e)
        {
            
        }
        if (resultMap == null)
            return buildResultMap();
        return resultMap;
    }
    
    private String buildResultMap()
    {
        return ResultMapBuilder.build(configuration, entityClass, namespace);
    }
    
    public static Map<String, ShardKeyGenerator> getShardedKeyGenerators()
    {
        return shardedKeyGenerators;
    }
    
    public static Map<String, SnGenerator> getSnGenerators()
    {
        return snGenerators;
    }
    
    public static Map<String, ShardSnGenerator> getShardSnGenerators()
    {
        return shardSnGenerators;
    }
    
}
