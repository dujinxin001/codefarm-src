package com.codefarm.mybatis.orm.builder;

import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.scripting.xmltags.ForEachSqlNode;
import org.apache.ibatis.scripting.xmltags.SqlNode;
import org.apache.ibatis.scripting.xmltags.TextSqlNode;
import org.apache.ibatis.session.Configuration;

import com.codefarm.mybatis.orm.annotations.Column;
import com.codefarm.mybatis.orm.annotations.Id;
import com.codefarm.spring.modules.util.CaseFormatUtils;

public abstract class SqlNodeBuilder
{
    protected static final String ITEM = "item";
    
    public abstract SqlNode build(Configuration configuration,
            MappedMethod method);
    
    /**
     * 获取Field对应的列名
     * @param field
     * @return
     */
    protected String getColumnNameByField(Field field)
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
    
    protected SqlNode buildForEachSqlNode(Configuration configuration,
            String collection)
    {
        TextSqlNode fieldSqlNode = new TextSqlNode("#{" + ITEM + "}");
        ForEachSqlNode forEachSqlNode = new ForEachSqlNode(configuration,
                fieldSqlNode, collection, "index", ITEM, "(", ")", ",");
        return forEachSqlNode;
    }
    
    protected String buildIfTest(String prefix, Field field)
    {
        Column column = field.getAnnotation(Column.class);
        if (column != null && StringUtils.isNotBlank(column.test()))
        {
            return column.test();
        }
        else
        {
            return prefix + field.getName() + "!=null";
            
        }
        
    }
    
    protected String getCollectionName(Parameter parameter)
    
    {
        //        Method method = methods.get(0);
        Class<?> parameterType = parameter.getType();
        if (parameterType.equals(List.class))
            return "list";
        else
            return "array";
    }
}
