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
import java.util.Date;
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
import org.apache.ibatis.scripting.xmltags.ForEachSqlNode;
import org.apache.ibatis.scripting.xmltags.IfSqlNode;
import org.apache.ibatis.scripting.xmltags.MixedSqlNode;
import org.apache.ibatis.scripting.xmltags.SetSqlNode;
import org.apache.ibatis.scripting.xmltags.SqlNode;
import org.apache.ibatis.scripting.xmltags.TextSqlNode;
import org.apache.ibatis.scripting.xmltags.TrimSqlNode;
import org.apache.ibatis.session.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.FieldCallback;
import org.springframework.util.ReflectionUtils.FieldFilter;

import com.codefarm.mybatis.orm.ConfigurationProperties;
import com.codefarm.mybatis.orm.annotations.BatchDelete;
import com.codefarm.mybatis.orm.annotations.BatchInsert;
import com.codefarm.mybatis.orm.annotations.BatchUpdate;
import com.codefarm.mybatis.orm.annotations.Column;
import com.codefarm.mybatis.orm.annotations.Delete;
import com.codefarm.mybatis.orm.annotations.Entity;
import com.codefarm.mybatis.orm.annotations.GeneratedValue;
import com.codefarm.mybatis.orm.annotations.GenerationType;
import com.codefarm.mybatis.orm.annotations.Get;
import com.codefarm.mybatis.orm.annotations.Id;
import com.codefarm.mybatis.orm.annotations.Insert;
import com.codefarm.mybatis.orm.annotations.MultiGet;
import com.codefarm.mybatis.orm.annotations.Sn;
import com.codefarm.mybatis.orm.annotations.Table;
import com.codefarm.mybatis.orm.annotations.Transient;
import com.codefarm.mybatis.orm.annotations.Update;
import com.codefarm.mybatis.orm.annotations.Version;
import com.codefarm.mybatis.orm.keygen.Jdbc4KeyGenerator;
import com.codefarm.mybatis.orm.keygen.SequenceKeyGenerator;
import com.codefarm.mybatis.orm.keygen.ShardJdbc4KeyGenerator;
import com.codefarm.mybatis.orm.keygen.ShardKeyGenerator;
import com.codefarm.mybatis.orm.keygen.ShardSnGenerator;
import com.codefarm.mybatis.orm.keygen.ShardUuidKeyGenerator;
import com.codefarm.mybatis.orm.keygen.SnGenerator;
import com.codefarm.mybatis.orm.keygen.UuidKeyGenerator;
import com.codefarm.spring.modules.util.AnnotationUtils;
import com.codefarm.spring.modules.util.CaseFormatUtils;
import com.codefarm.spring.modules.util.Collections3;
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
     * 实体对应的数据库表名
     */
    private String tableName;
    
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
    
    private static final String ITEM = "item";
    
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
        Table table = entityClass.getAnnotation(Table.class);
        //默认表名，大写字母转为下划线：TestUserEntity==>test_user_entity
        if (table == null)
        {
            tableName = CaseFormatUtils
                    .camelToUnderScore(entityClass.getSimpleName());
        }
        else
        {
            tableName = table.name();
        }
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
    
    private String getColumnNameByField(Field field)
    {
        Column column = field.getAnnotation(Column.class);
        if (column == null)
        {
            Id idColumn = field.getAnnotation(Id.class);
            if (idColumn != null)
                return StringUtils.isNotBlank(idColumn.column())
                        ? idColumn.column()
                        : CaseFormatUtils.camelToUnderScore(field.getName());
            return CaseFormatUtils.camelToUnderScore(field.getName());
        }
        else
        {
            return StringUtils.isNotBlank(column.name()) ? column.name()
                    : CaseFormatUtils.camelToUnderScore(field.getName());
        }
    }
    
    private String getTestByField(String prefix, Field field)
    {
        Column column = field.getAnnotation(Column.class);
        if (column != null && StringUtils.isNotBlank(column.test()))
        {
            return column.test();
        }
        else
        {
            return (StringUtils.isEmpty(prefix) ? "" : prefix + ".")
                    + field.getName() + "!= null";
        }
        
    }
    
    private String getIdColumnName()
    {
        Id id = idField.getAnnotation(Id.class);
        return StringUtils.isNotBlank(id.column()) ? id.column()
                : CaseFormatUtils.camelToUnderScore(idField.getName());
    }
    
    private String getIdFieldName()
    {
        return idField.getName();
    }
    
    private String getVersionSQL()
    {
        if (versionField != null)
        {
            return " AND " + getColumnNameByField(versionField) + " = #{"
                    + versionField.getName() + "}";
        }
        
        return StringUtils.EMPTY;
    }
    
    public void build()
    {
        String deleteStatementId = "delete";
        String updateStatementId = "update";
        String selectStatementId = "get";
        String batchInsertStatementId = "batchinsert";
        String batchDeleteStatementId = "batchDelete";
        String multiGetStatementId = "multiGet";
        
        buildInsertStatements();
        
        List<Method> deleteMethods = ReflectUtils
                .findMethodsAnnotatedWith(mapperType, Delete.class);
        if (Collections3.isNotEmpty(deleteMethods))
        {
            if (deleteMethods.size() > 1)
            {
                throw new RuntimeException("有多个@Delete方法");
            }
            deleteStatementId = deleteMethods.get(0).getName();
            if (!super.getConfiguration()
                    .hasStatement(namespace + "." + deleteStatementId))
            {
                buildDelete(namespace + "." + deleteStatementId);
            }
        }
        
        List<Method> updateMethods = ReflectUtils
                .findMethodsAnnotatedWith(mapperType, Update.class);
        if (Collections3.isNotEmpty(updateMethods))
        {
            if (updateMethods.size() > 1 && updateMethods.size() > 0)
            {
                throw new RuntimeException("有多个@Update方法");
            }
            updateStatementId = updateMethods.get(0).getName();
            if (!super.getConfiguration()
                    .hasStatement(namespace + "." + updateStatementId))
            {
                buildUpdate(namespace + "." + updateStatementId);
            }
        }
        
        buildSelectStatements();
        
        List<Method> batchInsertMethods = ReflectUtils
                .findMethodsAnnotatedWith(mapperType, BatchInsert.class);
        if (Collections3.isNotEmpty(batchInsertMethods))
        {
            if (batchInsertMethods.size() > 1)
            {
                throw new RuntimeException("有多个@BatchInsert方法");
            }
            batchInsertStatementId = batchInsertMethods.get(0).getName();
            if (!super.getConfiguration()
                    .hasCache(namespace + "." + batchInsertStatementId))
            {
                buildBatchInsert(namespace + "." + batchInsertStatementId,
                        getCollection(batchInsertMethods.get(0)));
            }
        }
        List<Method> batchDeleteMethods = ReflectUtils
                .findMethodsAnnotatedWith(mapperType, BatchDelete.class);
        if (Collections3.isNotEmpty(batchDeleteMethods))
        {
            if (batchDeleteMethods.size() > 1)
            {
                throw new RuntimeException("有多个@BatchDelete方法");
            }
            batchDeleteStatementId = batchDeleteMethods.get(0).getName();
            if (!super.getConfiguration()
                    .hasCache(namespace + "." + batchDeleteStatementId))
            {
                buildBatchDelete(namespace + "." + batchDeleteStatementId,
                        getCollection(batchDeleteMethods.get(0)));
            }
        }
        List<Method> batchUpdateMethods = ReflectUtils
                .findMethodsAnnotatedWith(mapperType, BatchUpdate.class);
        if (Collections3.isNotEmpty(batchUpdateMethods))
        {
            if (batchUpdateMethods.size() > 1)
            {
                throw new RuntimeException("有多个@BatchUpdate方法");
            }
            batchDeleteStatementId = batchUpdateMethods.get(0).getName();
            if (!super.getConfiguration()
                    .hasCache(namespace + "." + batchDeleteStatementId))
            {
                buildBatchUpdate(namespace + "." + batchDeleteStatementId,
                        getCollection(batchUpdateMethods.get(0)));
            }
        }
        List<Method> multiGetMethods = ReflectUtils
                .findMethodsAnnotatedWith(mapperType, MultiGet.class);
        if (Collections3.isNotEmpty(multiGetMethods))
        {
            if (multiGetMethods.size() > 1)
            {
                throw new RuntimeException("有多个@MultiGet方法");
            }
            multiGetStatementId = multiGetMethods.get(0).getName();
            if (!super.getConfiguration()
                    .hasCache(namespace + "." + multiGetStatementId))
            {
                buildMultiGet(namespace + "." + multiGetStatementId,
                        getCollection(multiGetMethods.get(0)));
            }
        }
    }
    
    private void buildSelectStatements()
    {
        String selectStatementId;
        List<Method> selectMethods = ReflectUtils
                .findMethodsAnnotatedWith(mapperType, Get.class);
        for (Method method : selectMethods)
        {
            selectStatementId = method.getName();
            if (!super.getConfiguration()
                    .hasStatement(namespace + "." + selectStatementId))
            {
                buildSelect(namespace + "." + selectStatementId);
            }
        }
    }
    
    private void buildInsertStatements()
    {
        String insertStatementId;
        List<Method> insertMethods = ReflectUtils
                .findMethodsAnnotatedWith(mapperType, Insert.class);
        for (Method method : insertMethods)
        {
            insertStatementId = method.getName();
            if (!super.getConfiguration()
                    .hasStatement(namespace + "." + insertStatementId))
            {
                buildInsert(namespace + "." + insertStatementId);
            }
        }
    }
    
    private String getCollection(Method method)
    
    {
        //        Method method = methods.get(0);
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length != 1)
            throw new RuntimeException("@Batch有且仅能有一个参数");
        Class<?> parameterType = parameterTypes[0];
        if (parameterType.equals(List.class))
            return "list";
        else
            return "array";
    }
    
    private void buildMultiGet(String statementId, String collection)
    {
        Integer fetchSize = null;
        Integer timeout = null;
        Class<?> resultType = entityClass;
        //~~~~~~~~~~~~~~~~~~~~~~~
        boolean flushCache = true;
        boolean useCache = false;
        boolean resultOrdered = false;
        KeyGenerator keyGenerator = new NoKeyGenerator();
        
        SqlSource sqlSource = new DynamicSqlSource(configuration,
                getMultiGetSql(collection));
        
        String resultMap = findResultMap();
        assistant.addMappedStatement(statementId,
                sqlSource,
                StatementType.PREPARED,
                SqlCommandType.SELECT,
                fetchSize,
                timeout,
                null,
                idField.getType(),
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
    
    private void buildBatchDelete(String statementId, String collection)
    {
        Integer timeout = null;
        Class<?> parameterType = idField.getType();
        
        //~~~~~~~~~~~~~~~~~~~~~~~
        boolean flushCache = true;
        boolean useCache = false;
        boolean resultOrdered = false;
        KeyGenerator keyGenerator = new NoKeyGenerator();
        
        SqlSource sqlSource = new DynamicSqlSource(configuration,
                getBatchDeleteSql(collection));
        
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
    
    private void buildBatchInsert(String statementId, String collection)
    {
        Integer fetchSize = null;
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
                    snGenerators.put(statementId, new SnGenerator());
                
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
                    shardSnGenerators.put(statementId, new ShardSnGenerator());
                if (generatedValue != null)
                {
                    if (generatedValue.strategy() == GenerationType.UUID)
                    {
                        shardedKeyGenerators.put(statementId,
                                new ShardUuidKeyGenerator(
                                        generatedValue.length()));
                    }
                    else if (generatedValue.strategy() == GenerationType.TABLE
                            || generatedValue.strategy() == GenerationType.AUTO)
                    {
                        shardedKeyGenerators.put(statementId,
                                new ShardJdbc4KeyGenerator());
                    }
                }
            }
            keyProperty = idField.getName();
            keyColumn = StringUtils.isBlank(id.column())
                    ? CaseFormatUtils.camelToUnderScore(idField.getName())
                    : id.column();
        }
        
        List<SqlNode> contents = new ArrayList<SqlNode>();
        contents.add(this.getBatchInsertSql(collection));
        SqlSource sqlSource = new DynamicSqlSource(configuration,
                new MixedSqlNode(contents));
        
        assistant.addMappedStatement(statementId,
                sqlSource,
                StatementType.PREPARED,
                SqlCommandType.INSERT,
                fetchSize,
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
                keyProperty,
                keyColumn,
                databaseId,
                lang);
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
    
    private void buildInsert(String statementId)
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
        
        if (id != null && id.generatedKeys())
        {
            String keyStatementId = entityClass.getName() + ".insert"
                    + SelectKeyGenerator.SELECT_KEY_SUFFIX;
            if (!sharded)
            {
                
                keyGenerator = buildKeyGenerator(statementId, keyStatementId);
            }
            else
            {
                buildShardedKeyGenerator(statementId);
            }
            keyProperty = idField.getName();
            keyColumn = StringUtils.isBlank(id.column())
                    ? CaseFormatUtils.camelToUnderScore(idField.getName())
                    : id.column();
        }
        
        List<SqlNode> contents = new ArrayList<SqlNode>();
        contents.add(this.getInsertSql());
        SqlSource sqlSource = new DynamicSqlSource(configuration,
                new MixedSqlNode(contents));
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
                return new SequenceKeyGenerator(generatedValue.sequence());
            }
        }
        return new NoKeyGenerator();
    }
    
    private SqlNode getBatchInsertSql(String collection)
    {
        List<SqlNode> contents = new ArrayList<SqlNode>();
        contents.add(new TextSqlNode("INSERT INTO " + tableName + " "));
        contents.add(getBatchInsertColumns());
        contents.add(getBatchInsertFields(collection));
        return new MixedSqlNode(contents);
    }
    
    private SqlNode getBatchDeleteSql(String collection)
    {
        List<SqlNode> contents = new ArrayList<SqlNode>();
        contents.add(new TextSqlNode("DELETE FROM " + tableName + " WHERE "
                + getIdColumnName() + " in "));
        contents.add(getBatchDeleteFields(collection));
        return new MixedSqlNode(contents);
    }
    
    private SqlNode getMultiGetSql(String collection)
    {
        String sql = "SELECT " + getIdColumnName() + " AS " + getIdFieldName();
        
        for (Field field : columnFields)
        {
            if (!getColumnNameByField(field).equals(getIdColumnName()))
                sql += "," + getColumnNameByField(field) + " AS "
                        + getColumnNameByField(field);
        }
        
        sql += " FROM " + tableName + " WHERE " + getIdColumnName() + " in";
        List<SqlNode> contents = new ArrayList<SqlNode>();
        contents.add(new TextSqlNode(sql));
        contents.add(getMultiGetFields(collection));
        return new MixedSqlNode(contents);
    }
    
    private SqlNode getMultiGetFields(String collection)
    {
        TextSqlNode fieldSqlNode = new TextSqlNode("#{" + ITEM + "}");
        ForEachSqlNode forEachSqlNode = new ForEachSqlNode(configuration,
                fieldSqlNode, collection, "index", ITEM, "(", ")", ",");
        return forEachSqlNode;
    }
    
    private SqlNode getBatchDeleteFields(String collection)
    {
        TextSqlNode fieldSqlNode = new TextSqlNode("#{" + ITEM + "}");
        ForEachSqlNode forEachSqlNode = new ForEachSqlNode(configuration,
                fieldSqlNode, collection, "index", ITEM, "(", ")", ",");
        return forEachSqlNode;
    }
    
    private SqlNode getBatchInsertFields(String collection)
    {
        List<SqlNode> contents = new ArrayList<SqlNode>();
        for (Field field : columnFields)
        {
            List<SqlNode> sqlNodes = new ArrayList<SqlNode>();
            Column column = field.getAnnotation(Column.class);
            if (Date.class.isAssignableFrom(field.getType()) && column != null
                    && column.sysdate() == true)
            {
                sqlNodes.add(new TextSqlNode("now(),"));
            }
            else
            {
                sqlNodes.add(
                        new TextSqlNode("#{item." + field.getName() + "},"));
            }
            
            contents.add(new MixedSqlNode(sqlNodes));
        }
        TrimSqlNode fieldSqlNode = new TrimSqlNode(configuration,
                new MixedSqlNode(contents), " (", null, ")", ",");
        
        ForEachSqlNode forEachSqlNode = new ForEachSqlNode(configuration,
                fieldSqlNode, collection, "index", ITEM, "", "", ",");
        
        return new TrimSqlNode(configuration, forEachSqlNode, " VALUES ", null,
                "", ",");
    }
    
    private SqlNode getInsertSql()
    {
        List<SqlNode> contents = new ArrayList<SqlNode>();
        contents.add(new TextSqlNode("INSERT INTO " + tableName + " "));
        contents.add(getInsertColumns());
        contents.add(getInsertFileds());
        return new MixedSqlNode(contents);
    }
    
    private SqlNode getInsertFileds()
    {
        List<SqlNode> contents = new ArrayList<SqlNode>();
        for (Field field : columnFields)
        {
            List<SqlNode> sqlNodes = new ArrayList<SqlNode>();
            Column column = field.getAnnotation(Column.class);
            if (Date.class.isAssignableFrom(field.getType()) && column != null
                    && column.sysdate() == true)
            {
                sqlNodes.add(new TextSqlNode("now(),"));
            }
            else
            {
                sqlNodes.add(new TextSqlNode("#{" + field.getName() + "},"));
            }
            
            contents.add(new IfSqlNode(new MixedSqlNode(sqlNodes),
                    getTestByField(null, field)));
        }
        
        return new TrimSqlNode(configuration, new MixedSqlNode(contents),
                " VALUES (", null, ")", ",");
    }
    
    private TrimSqlNode getBatchInsertColumns()
    {
        List<SqlNode> contents = new ArrayList<SqlNode>();
        for (Field field : columnFields)
        {
            List<SqlNode> sqlNodes = new ArrayList<SqlNode>();
            sqlNodes.add(new TextSqlNode(getColumnNameByField(field) + ","));
            
            contents.add(new MixedSqlNode(sqlNodes));
        }
        
        return new TrimSqlNode(configuration, new MixedSqlNode(contents), "(",
                null, ")", ",");
    }
    
    private TrimSqlNode getInsertColumns()
    {
        List<SqlNode> contents = new ArrayList<SqlNode>();
        for (Field field : columnFields)
        {
            List<SqlNode> sqlNodes = new ArrayList<SqlNode>();
            sqlNodes.add(new TextSqlNode(getColumnNameByField(field) + ","));
            
            contents.add(new IfSqlNode(new MixedSqlNode(sqlNodes),
                    getTestByField(null, field)));
        }
        
        return new TrimSqlNode(configuration, new MixedSqlNode(contents), "(",
                null, ")", ",");
    }
    
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //delete
    private void buildDelete(String statementId)
    {
        Integer timeout = null;
        Class<?> parameterType = entityClass;
        
        //~~~~~~~~~~~~~~~~~~~~~~~
        boolean flushCache = true;
        boolean useCache = false;
        boolean resultOrdered = false;
        KeyGenerator keyGenerator = new NoKeyGenerator();
        List<SqlNode> contents = new ArrayList<SqlNode>();
        SqlNode sqlNode = new TextSqlNode("DELETE FROM " + tableName + " WHERE "
                + getIdColumnName() + " = #{" + getIdFieldName() + "} ");
        contents.add(sqlNode);
        //        if (versionField != null)
        //            contents.add(new IfSqlNode(new TextSqlNode(getVersionSQL()),
        //                    getTestByField(null, versionField)));
        SqlSource sqlSource = new DynamicSqlSource(configuration,
                new MixedSqlNode(contents));
        
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
    
    private void buildBatchUpdate(String statementId, String collection)
    {
        Integer timeout = null;
        Class<?> parameterType = entityClass;
        
        //~~~~~~~~~~~~~
        boolean flushCache = true;
        boolean useCache = false;
        boolean resultOrdered = false;
        KeyGenerator keyGenerator = new NoKeyGenerator();
        
        List<SqlNode> contents = new ArrayList<SqlNode>();
        contents.add(this.getBatchUpdateSql(collection));
        
        SqlSource sqlSource = new DynamicSqlSource(configuration,
                new MixedSqlNode(contents));
        String parameterMap = findParameterMap();
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
    
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //update
    private void buildUpdate(String statementId)
    {
        Integer timeout = null;
        Class<?> parameterType = entityClass;
        
        //~~~~~~~~~~~~~
        boolean flushCache = true;
        boolean useCache = false;
        boolean resultOrdered = false;
        KeyGenerator keyGenerator = new NoKeyGenerator();
        
        List<SqlNode> contents = new ArrayList<SqlNode>();
        contents.add(this.getUpdateSql());
        
        SqlSource sqlSource = new DynamicSqlSource(configuration,
                new MixedSqlNode(contents));
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
    
    private SqlNode getUpdateSql()
    {
        List<SqlNode> contents = new ArrayList<SqlNode>();
        contents.add(new TextSqlNode("UPDATE " + tableName + " "));
        contents.add(getUpdateColumns());
        
        contents.add(new TextSqlNode(" WHERE " + getIdColumnName() + " = #{"
                + getIdFieldName() + "}"));
        if (versionField != null)
            contents.add(new IfSqlNode(new TextSqlNode(getVersionSQL()),
                    getTestByField(null, versionField)));
        return new MixedSqlNode(contents);
    }
    
    private SqlNode getBatchUpdateSql(String collection)
    {
        List<SqlNode> contents = new ArrayList<SqlNode>();
        contents.add(new TextSqlNode("UPDATE " + tableName + " "));
        contents.add(getBatchUpdateColumns());
        
        contents.add(new TextSqlNode(" WHERE " + getIdColumnName() + " = #{"
                + ITEM + "." + getIdFieldName() + "}"));
        if (versionField != null)
            contents.add(new IfSqlNode(new TextSqlNode(getVersionSQL()),
                    getTestByField(null, versionField)));
        MixedSqlNode mixedSqlNode = new MixedSqlNode(contents);
        return new ForEachSqlNode(configuration, mixedSqlNode, collection,
                "index", ITEM, "", "", ";");
    }
    
    private SqlNode getBatchUpdateColumns()
    {
        List<SqlNode> contents = new ArrayList<SqlNode>();
        for (Field field : columnFields)
        {
            List<SqlNode> sqlNodes = new ArrayList<SqlNode>();
            if (Date.class.isAssignableFrom(field.getType())
                    && field.getAnnotation(Column.class) != null
                    && field.getAnnotation(Column.class).sysdate() == true)
            {
                sqlNodes.add(new TextSqlNode(
                        getColumnNameByField(field) + " = now(),"));
            }
            else
            {
                sqlNodes.add(new TextSqlNode(getColumnNameByField(field)
                        + " = #{" + ITEM + "." + field.getName() + "},"));
            }
            
            contents.add(new IfSqlNode(new MixedSqlNode(sqlNodes),
                    getTestByField(ITEM, field)));
        }
        if (versionField != null)
            contents.add(new TextSqlNode(getColumnNameByField(versionField)
                    + "=" + getColumnNameByField(versionField) + "+1"));
        
        return new SetSqlNode(configuration, new MixedSqlNode(contents));
    }
    
    private SqlNode getUpdateColumns()
    {
        List<SqlNode> contents = new ArrayList<SqlNode>();
        for (Field field : columnFields)
        {
            List<SqlNode> sqlNodes = new ArrayList<SqlNode>();
            
            if (Date.class.isAssignableFrom(field.getType())
                    && field.getAnnotation(Column.class) != null
                    && field.getAnnotation(Column.class).sysdate() == true)
            {
                sqlNodes.add(new TextSqlNode(
                        getColumnNameByField(field) + " = now(),"));
            }
            else if (!field.isAnnotationPresent(Version.class))
            {
                sqlNodes.add(new TextSqlNode(getColumnNameByField(field)
                        + " = #{" + field.getName() + "},"));
            }
            
            contents.add(new IfSqlNode(new MixedSqlNode(sqlNodes),
                    getTestByField(null, field)));
        }
        if (versionField != null)
        {
            contents.add(new TextSqlNode(getColumnNameByField(versionField)
                    + "=" + getColumnNameByField(versionField) + "+1"));
        }
        
        return new SetSqlNode(configuration, new MixedSqlNode(contents));
    }
    
    //~~~~~~~~~~~~~~~~~
    //get
    private void buildSelect(String statementId)
    {
        Integer fetchSize = null;
        Integer timeout = entity.timeout() == -1 ? null : entity.timeout();
        Class<?> resultType = entityClass;
        
        boolean flushCache = entity.flushCache();
        boolean useCache = entity.useCache();
        boolean resultOrdered = false;
        KeyGenerator keyGenerator = new NoKeyGenerator();
        
        List<SqlNode> contents = new ArrayList<SqlNode>();
        contents.add(this.getGetSql());
        SqlSource sqlSource = new DynamicSqlSource(configuration,
                new MixedSqlNode(contents));
        
        String resultMap = findResultMap();
        assistant.addMappedStatement(statementId,
                sqlSource,
                StatementType.PREPARED,
                SqlCommandType.SELECT,
                fetchSize,
                timeout,
                null,
                idField.getType(),
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
    
    private String findResultMap()
    {
        String resultMap = null;
        Iterator<String> resultMapNames = configuration.getResultMapNames()
                .iterator();
        while (resultMapNames.hasNext())
        {
            String name = resultMapNames.next();
            ResultMap temp = configuration.getResultMap(name);
            if (temp.getType().equals(entityClass))
            {
                resultMap = temp.getId();
                break;
            }
        }
        return resultMap;
    }
    
    private SqlNode getGetSql()
    {
        String sql = "SELECT " + getIdColumnName() + " AS " + getIdFieldName();
        
        for (Field field : columnFields)
        {
            if (!getColumnNameByField(field).equals(getIdColumnName()))
                sql += "," + getColumnNameByField(field) + " AS "
                        + getColumnNameByField(field);
        }
        
        sql += " FROM " + tableName + " WHERE " + getIdColumnName() + " = #{"
                + getIdFieldName() + "}";
        
        return new TextSqlNode(sql);
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
