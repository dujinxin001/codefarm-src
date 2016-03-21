package com.codefarm.mybatis.orm;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.builder.IncompleteElementException;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.builder.ResultMapResolver;
import org.apache.ibatis.mapping.ResultFlag;
import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.JdbcType;

import com.codefarm.mybatis.orm.annotations.Column;
import com.codefarm.mybatis.orm.annotations.Id;

public class ResultMapBuilder
{
    public static String build(Configuration configuration, Class<?> entity,
            String namespace)
    {
        List<ResultFlag> flags = new ArrayList<>();
        MapperBuilderAssistant builderAssistant = new MapperBuilderAssistant(
                configuration, null);
        builderAssistant.setCurrentNamespace(namespace);
        List<ResultMapping> resultMappings = new ArrayList<ResultMapping>();
        for (Field field : entity.getDeclaredFields())
        {
            String property = field.getName();
            Class<?> javaType = field.getType();
            String columnName = null;
            JdbcType jdbcType = null;
            if (field.isAnnotationPresent(Column.class))
            {
                Column column = field.getAnnotation(Column.class);
                
                columnName = field.getName();
                jdbcType = column.jdbcType();
            }
            else if (field.isAnnotationPresent(Id.class))
            {
                Id id = field.getAnnotation(Id.class);
                columnName = field.getName();
                jdbcType = id.jdbcType();
                flags.add(ResultFlag.ID);
            }
            else
                continue;
            resultMappings.add(builderAssistant.buildResultMapping(entity,
                    property,
                    columnName,
                    javaType,
                    jdbcType,
                    null,
                    null,
                    null,
                    null,
                    null,
                    flags,
                    null,
                    null,
                    false));
        }
        ResultMapResolver resultMapResolver = new ResultMapResolver(
                builderAssistant, "baseRs", entity, null, null, resultMappings,
                null);
        try
        {
            return resultMapResolver.resolve().getId();
        }
        catch (IncompleteElementException e)
        {
            configuration.addIncompleteResultMap(resultMapResolver);
            throw e;
        }
    }
}
