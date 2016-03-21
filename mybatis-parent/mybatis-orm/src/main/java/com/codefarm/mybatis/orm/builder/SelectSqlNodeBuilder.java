package com.codefarm.mybatis.orm.builder;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.scripting.xmltags.MixedSqlNode;
import org.apache.ibatis.scripting.xmltags.SqlNode;
import org.apache.ibatis.scripting.xmltags.TextSqlNode;
import org.apache.ibatis.scripting.xmltags.TrimSqlNode;
import org.apache.ibatis.session.Configuration;

import com.codefarm.mybatis.orm.annotations.Select;

/**
 * 查询sql构造器
 * @author zhangjian
 *
 */
public class SelectSqlNodeBuilder extends SqlNodeBuilder
{
    
    @Override
    public SqlNode build(Configuration configuration, MappedMethod method)
    {
        List<SqlNode> contents = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ");
        //        String sql = "SELECT " + getIdColumnName() + " AS " + getIdFieldName();
        
        for (Field field : method.getColumnFields())
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
        sb.append(method.getTable());
        contents.add(new TextSqlNode(sb.toString()));
        contents.add(new TrimSqlNode(configuration,
                new CriteriaSqlNodeBuilder().build(configuration, method), null,
                null, null, "AND"));
        Select select = method.getMethod().getAnnotation(Select.class);
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
    
}
