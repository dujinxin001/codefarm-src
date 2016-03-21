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
import java.lang.reflect.Parameter;
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
import com.codefarm.mybatis.orm.ResultMapBuilder;
import com.codefarm.mybatis.orm.annotations.Column;
import com.codefarm.mybatis.orm.annotations.Criteria;
import com.codefarm.mybatis.orm.annotations.Criterias;
import com.codefarm.mybatis.orm.annotations.Delete;
import com.codefarm.mybatis.orm.annotations.Entity;
import com.codefarm.mybatis.orm.annotations.GeneratedValue;
import com.codefarm.mybatis.orm.annotations.GenerationType;
import com.codefarm.mybatis.orm.annotations.Id;
import com.codefarm.mybatis.orm.annotations.Insert;
import com.codefarm.mybatis.orm.annotations.Select;
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
    
    /**
     * 生成if动态sql
     * @param prefix
     * @param field
     * @return
     */
    private String getTestByField(String prefix, Field field)
    {
        Column column = field.getAnnotation(Column.class);
        if (column != null && StringUtils.isNotBlank(column.test()))
        {
            return column.test();
        }
        else
        {
            if (StringUtils.isEmpty(prefix))
                return field.getName() + "!=null";
            else
            {
                return prefix + "." + field.getName() + "!= null";
            }
            
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
    
    private String getCollectionName(Method method)
    
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
    
    private String getCollectionName(Parameter parameter)
    
    {
        //        Method method = methods.get(0);
        Class<?> parameterType = parameter.getType();
        if (parameterType.equals(List.class))
            return "list";
        else
            return "array";
    }
    
    private String getCollectionName(Field field)
    {
        Class<?> parameterType = field.getDeclaringClass();
        if (parameterType.equals(List.class))
            return "list";
        else
            return "array";
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
        contents.add(this.getInsertSql(method));
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
    
    private SqlNode getForEachSqlNode(String collection)
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
                if (column != null)
                    sqlNodes.add(
                            new TextSqlNode("#{" + ITEM + "." + field.getName()
                                    + ",jdbcType=" + column.jdbcType() + "},"));
                else
                {
                    Id id = field.getAnnotation(Id.class);
                    sqlNodes.add(
                            new TextSqlNode("#{" + ITEM + "." + field.getName()
                                    + ",jdbcType=" + id.jdbcType() + "},"));
                }
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
    
    private SqlNode getInsertSql(Method method)
    {
        if (method.getParameters().length > 1 || method.getParameters() == null)
            throw new RuntimeException("inserts can have only one parameter");
        List<SqlNode> contents = new ArrayList<SqlNode>();
        contents.add(new TextSqlNode("INSERT INTO " + tableName + " "));
        contents.add(getInsertColumns());
        Class<?> parameterType = method.getParameterTypes()[0];
        if (parameterType.isArray()
                || Collection.class.isAssignableFrom(parameterType))
            contents.add(getBatchInsertFields(
                    getCollectionName(method.getParameters()[0])));
        else
            contents.add(getInsertFileds());
        return new MixedSqlNode(contents);
    }
    
    private SqlNode getInsertFileds()
    {
        List<SqlNode> contents = new ArrayList<SqlNode>();
        for (Field field : columnFields)
        {
            Column column = field.getAnnotation(Column.class);
            if (Date.class.isAssignableFrom(field.getType()) && column != null
                    && column.sysdate() == true)
            {
                contents.add(new TextSqlNode("now(),"));
            }
            else
            {
                if (column != null)
                    contents.add(new TextSqlNode("#{" + field.getName()
                            + ",jdbcType=" + column.jdbcType() + "},"));
                else
                {
                    Id id = field.getAnnotation(Id.class);
                    contents.add(new TextSqlNode("#{" + field.getName()
                            + ",jdbcType=" + id.jdbcType() + "},"));
                }
            }
            
        }
        
        return new TrimSqlNode(configuration, new MixedSqlNode(contents),
                " VALUES (", null, ")", ",");
    }
    
    private TrimSqlNode getInsertColumns()
    {
        List<SqlNode> contents = new ArrayList<SqlNode>();
        for (Field field : columnFields)
        {
            contents.add(new TextSqlNode(getColumnNameByField(field) + ","));
        }
        
        return new TrimSqlNode(configuration, new MixedSqlNode(contents), "(",
                null, ")", ",");
    }
    
    private SqlNode getDeleteSqlNode(Method method)
    {
        List<SqlNode> contents = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append("DELETE FROM ");
        sb.append(tableName);
        contents.add(new TextSqlNode(sb.toString()));
        contents.add(buildCriterias(method));
        return new MixedSqlNode(contents);
    }
    
    private void buildDelete(String statementId, Method method)
    {
        Integer timeout = null;
        Class<?> parameterType = entityClass;
        
        boolean flushCache = true;
        boolean useCache = false;
        boolean resultOrdered = false;
        KeyGenerator keyGenerator = new NoKeyGenerator();
        List<SqlNode> contents = new ArrayList<SqlNode>();
        SqlNode sqlNode = new TrimSqlNode(configuration,
                getDeleteSqlNode(method), null, null, null, "AND");
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
        
        List<SqlNode> contents = new ArrayList<SqlNode>();
        contents.add(this.getUpdateSql(method));
        
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
    
    private SqlNode getUpdateSql(Method method)
    {
        List<SqlNode> contents = new ArrayList<SqlNode>();
        contents.add(new TextSqlNode("UPDATE " + tableName + " "));
        contents.add(getUpdateColumns(method));
        //---------默认根据主键更新----------//
        if (method.getParameters().length == 1)
            contents.add(new TextSqlNode(" WHERE " + getIdColumnName() + " = #{"
                    + getIdFieldName() + "}"));
        else
            contents.add(buildCriterias(method));
        if (versionField != null)
            contents.add(new IfSqlNode(new TextSqlNode(getVersionSQL()),
                    getTestByField(null, versionField)));
        return new MixedSqlNode(contents);
    }
    
    private SqlNode getUpdateColumns(Method method)
    {
        List<SqlNode> contents = new ArrayList<SqlNode>();
        StringBuilder sb = new StringBuilder();
        String prefix = getPrefix(method);
        for (Field field : columnFields)
        {
            sb.delete(0, sb.length());
            if (field.equals(idField))
                continue;
            
            if (Date.class.isAssignableFrom(field.getType())
                    && field.getAnnotation(Column.class) != null
                    && field.getAnnotation(Column.class).sysdate() == true)
            {
                if (com.codefarm.spring.modules.util.StringUtils
                        .isNotEmpty(prefix))
                {
                    sb.append(prefix);
                    sb.append(".");
                }
                sb.append(getColumnNameByField(field));
                sb.append(" = now(),");
            }
            else if (!field.isAnnotationPresent(Version.class))
            {
                sb.append(getColumnNameByField(field));
                sb.append(" = #{");
                if (com.codefarm.spring.modules.util.StringUtils
                        .isNotEmpty(prefix))
                {
                    sb.append(prefix);
                    sb.append(".");
                }
                sb.append(field.getName());
                sb.append(",jdbcType=");
                sb.append(field.getAnnotation(Column.class).jdbcType());
                sb.append("},");
            }
            contents.add(new IfSqlNode(new TextSqlNode(sb.toString()),
                    getTestByField(prefix, field)));
        }
        if (versionField != null)
        {
            contents.add(new TextSqlNode(getColumnNameByField(versionField)
                    + "=" + getColumnNameByField(versionField) + "+1"));
        }
        
        return new SetSqlNode(configuration, new MixedSqlNode(contents));
    }
    
    private String getPrefix(Method method)
    {
        String prefix = "";
        Parameter[] parameters = method.getParameters();
        
        if (parameters.length > 1)
        {
            int index = 1;
            for (Parameter parameter : parameters)
            {
                if (parameter.getType().equals(entityClass))
                    prefix = "param" + index;
                index++;
            }
        }
        return prefix;
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
        
        List<SqlNode> contents = new ArrayList<SqlNode>();
        contents.add(this.getSelectSql(method));
        SqlSource sqlSource = new DynamicSqlSource(configuration,
                new MixedSqlNode(contents));
        
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
    
    private SqlNode getSelectSql(Method method)
    {
        List<SqlNode> contents = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ");
        //        String sql = "SELECT " + getIdColumnName() + " AS " + getIdFieldName();
        
        for (Field field : columnFields)
        {
            sb.append(getColumnNameByField(field));
            sb.append(" AS ");
            sb.append(field.getName());
            sb.append(",");
        }
        contents.add(new TrimSqlNode(configuration,
                new TextSqlNode(sb.toString()), null, null, null, ","));
        sb.delete(0, sb.length());
        sb.append(" FROM ");
        sb.append(tableName);
        contents.add(new TextSqlNode(sb.toString()));
        //        sql += " FROM " + tableName + " WHERE " + getIdColumnName() + " = #{"
        //                + getIdFieldName() + "}";
        //        SqlNode clause = new TextSqlNode(sb.toString());
        //                content.add(new IfSqlNode(clause,
        //                        getTestByParameter(null, parameter)));
        contents.add(new TrimSqlNode(configuration, buildCriterias(method),
                null, null, null, "AND"));
        Select select = method.getAnnotation(Select.class);
        if (com.codefarm.spring.modules.util.StringUtils
                .isNotEmpty(select.orderby()))
        {
            sb.delete(0, sb.length());
            sb.append(" order by ");
            sb.append(select.orderby());
            contents.add(new TextSqlNode(sb.toString()));
        }
        return new MixedSqlNode(contents);
    }
    
    private SqlNode buildCriterias(Method method)
    {
        List<SqlNode> contents = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        Parameter[] parameters = method.getParameters();
        if (parameters != null && parameters.length > 0)
            contents.add(new TextSqlNode(" WHERE "));
        int index = 1;
        for (Parameter parameter : parameters)
        {
            sb.delete(0, sb.length());
            //-------parse @Criterias parameter
            if (parameter.getType().isAnnotationPresent(Criterias.class))
            {
                Field[] fields = parameter.getType().getDeclaredFields();
                for (Field field : fields)
                {
                    
                    sb.delete(0, sb.length());
                    if (field.isAnnotationPresent(Criteria.class))
                    {
                        Criteria annotation = field
                                .getAnnotation(Criteria.class);
                        String columnName = annotation.column();
                        //--------数组参数--------//
                        if (field.getType().isArray() || Collection.class
                                .isAssignableFrom(field.getDeclaringClass()))
                        {
                            sb.append(columnName);
                            sb.append(" in ");
                            List<SqlNode> sub = new ArrayList<>();
                            sub.add(new TextSqlNode(sb.toString()));
                            if (parameters.length > 1)
                                sub.add(getForEachSqlNode("param" + index + "."
                                        + field.getName()));
                            else
                                sub.add(getForEachSqlNode(field.getName()));
                            sub.add(new TextSqlNode(" AND "));
                            String test = getTestByField(parameters.length > 1
                                    ? "param" + index : null, field);
                            contents.add(
                                    new IfSqlNode(new MixedSqlNode(sub), test));
                            
                        }
                        else
                        {
                            sb.append(columnName);
                            sb.append(" ");
                            sb.append(annotation.operator().getOperator());
                            
                            sb.append(" #{");
                            if (parameters.length > 1)
                            {
                                sb.append("param" + index + ".");
                            }
                            sb.append(field.getName());
                            sb.append("}");
                            sb.append(" AND ");
                            String test = getTestByField(parameters.length > 1
                                    ? "param" + index : null, field);
                            contents.add(new IfSqlNode(
                                    new TextSqlNode(sb.toString()), test));
                        }
                        
                    }
                    
                }
                
            }
            //--------parse Primative paramter
            else if (parameter.isAnnotationPresent(Criteria.class))
            {
                Criteria annotation = parameter.getAnnotation(Criteria.class);
                String columnName = annotation.column();
                //--------数组参数--------//
                if (parameter.getType().isArray() || Collection.class
                        .isAssignableFrom(parameter.getType()))
                {
                    sb.append(columnName);
                    sb.append(" in ");
                    contents.add(new TextSqlNode(sb.toString()));
                    String collection = getCollectionName(parameter);
                    contents.add(getForEachSqlNode(collection));
                }
                else
                {
                    
                    sb.append(columnName);
                    sb.append(annotation.operator().getOperator());
                    sb.append(" #{");
                    if (parameters.length > 1)
                    {
                        sb.append("param" + index);
                    }
                    else
                        sb.append(index);
                    sb.append("}");
                    sb.append(" AND ");
                    contents.add(new TextSqlNode(sb.toString()));
                }
            }
            index++;
        }
        return new TrimSqlNode(configuration, new MixedSqlNode(contents), "",
                null, "", "AND");
        //        return new MixedSqlNode(contents);
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
