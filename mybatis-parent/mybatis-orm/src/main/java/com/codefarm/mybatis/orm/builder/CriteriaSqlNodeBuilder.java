package com.codefarm.mybatis.orm.builder;

import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.ibatis.scripting.xmltags.IfSqlNode;
import org.apache.ibatis.scripting.xmltags.MixedSqlNode;
import org.apache.ibatis.scripting.xmltags.SqlNode;
import org.apache.ibatis.scripting.xmltags.TextSqlNode;
import org.apache.ibatis.scripting.xmltags.TrimSqlNode;
import org.apache.ibatis.session.Configuration;

import com.codefarm.mybatis.orm.annotations.Criteria;
import com.codefarm.mybatis.orm.annotations.Criterias;

/**
 * where子句构造器
 * @author zhangjian
 *
 */
public class CriteriaSqlNodeBuilder extends SqlNodeBuilder
{
    private static final String PREFIX = "param";
    
    @Override
    public SqlNode build(Configuration configuration, MappedMethod method)
    {
        List<SqlNode> contents = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        Parameter[] parameters = method.getMethod().getParameters();
        if (parameters != null && parameters.length > 0)
            contents.add(new TextSqlNode(" WHERE "));
        //参数索引，从1开始
        int index = 1;
        String prefix = "";
        for (Parameter parameter : parameters)
        {
            if (parameters.length > 1)
                prefix = PREFIX + index + ".";
            sb.delete(0, sb.length());
            //-------parse @Criterias parameter Criterias对象参数
            if (parameter.getType().isAnnotationPresent(Criterias.class))
            {
                buildCriteriaFromObject(configuration,
                        contents,
                        prefix,
                        parameter);
                
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
                    contents.add(
                            buildForEachSqlNode(configuration, collection));
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
    }
    
    private void buildCriteriaFromObject(Configuration configuration,
            List<SqlNode> contents, String prefix, Parameter parameter)
    {
        StringBuilder sb = new StringBuilder();
        Field[] fields = parameter.getType().getDeclaredFields();
        for (Field field : fields)
        {
            if (!field.isAnnotationPresent(Criteria.class))
                continue;
            
            sb.delete(0, sb.length());
            Criteria annotation = field.getAnnotation(Criteria.class);
            String columnName = annotation.column();
            //--------数组参数--------//
            if (field.getType().isArray() || Collection.class
                    .isAssignableFrom(field.getDeclaringClass()))
            {
                sb.append(columnName);
                sb.append(" in ");
                List<SqlNode> sub = new ArrayList<>();
                sub.add(new TextSqlNode(sb.toString()));
                sub.add(buildForEachSqlNode(configuration,
                        prefix + field.getName()));
                sub.add(new TextSqlNode(" AND "));
                String test = buildIfTest(prefix, field);
                contents.add(new IfSqlNode(new MixedSqlNode(sub), test));
            }
            else
            {
                sb.append(columnName);
                sb.append(" ");
                sb.append(annotation.operator().getOperator());
                
                sb.append(" #{");
                sb.append(prefix);
                sb.append(field.getName());
                sb.append("}");
                sb.append(" AND ");
                String test = buildIfTest(prefix, field);
                contents.add(
                        new IfSqlNode(new TextSqlNode(sb.toString()), test));
            }
            
        }
    }
    
}
