package com.codefarm.mybatis.orm.builder;

import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.ibatis.scripting.xmltags.ForEachSqlNode;
import org.apache.ibatis.scripting.xmltags.MixedSqlNode;
import org.apache.ibatis.scripting.xmltags.SqlNode;
import org.apache.ibatis.scripting.xmltags.TextSqlNode;
import org.apache.ibatis.scripting.xmltags.TrimSqlNode;
import org.apache.ibatis.session.Configuration;

import com.codefarm.mybatis.orm.annotations.Column;
import com.codefarm.mybatis.orm.annotations.Id;

public class InsertSqlNodeBuilder extends SqlNodeBuilder
{
    
    @Override
    public SqlNode build(Configuration configuration, MappedMethod method)
    {
        if (method.getMethod().getParameters().length > 1
                || method.getMethod().getParameters() == null)
            throw new RuntimeException("inserts can have only one parameter");
        List<SqlNode> contents = new ArrayList<SqlNode>();
        contents.add(new TextSqlNode("INSERT INTO " + method.getTable() + " "));
        contents.add(buildInsertColumns(configuration, method));
        Class<?> parameterType = method.getMethod().getParameterTypes()[0];
        Parameter parameter = method.getMethod().getParameters()[0];
        if (parameterType.isArray()
                || Collection.class.isAssignableFrom(parameterType))
            contents.add(buildBatchValues(configuration,
                    method,
                    getCollectionName(parameter)));
        else
            contents.add(buildValues(configuration, method));
        return new MixedSqlNode(contents);
    }
    
    /**
     * 生成插入字段
     * @param configuration
     * @param method
     * @return
     */
    private TrimSqlNode buildInsertColumns(Configuration configuration,
            MappedMethod method)
    {
        List<SqlNode> contents = new ArrayList<SqlNode>();
        for (Field field : method.getColumnFields())
        {
            contents.add(new TextSqlNode(getColumnNameByField(field) + ","));
        }
        
        return new TrimSqlNode(configuration, new MixedSqlNode(contents), "(",
                null, ")", ",");
    }
    
    /**
     * 生成批量插入VALUES子句
     * @param configuration
     * @param method
     * @param collection
     * @return
     */
    private SqlNode buildBatchValues(Configuration configuration,
            MappedMethod method, String collection)
    {
        List<SqlNode> contents = new ArrayList<SqlNode>();
        for (Field field : method.getColumnFields())
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
    
    /**
     * 生成单行插入VALUES子句
     * @param configuration
     * @param method
     * @return
     */
    private SqlNode buildValues(Configuration configuration,
            MappedMethod method)
    {
        List<SqlNode> contents = new ArrayList<SqlNode>();
        for (Field field : method.getColumnFields())
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
    
}
