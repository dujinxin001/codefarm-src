package com.codefarm.mybatis.orm.builder;

import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.scripting.xmltags.MixedSqlNode;
import org.apache.ibatis.scripting.xmltags.SqlNode;
import org.apache.ibatis.scripting.xmltags.TextSqlNode;
import org.apache.ibatis.session.Configuration;

public class DeleteSqlNodeBuilder extends SqlNodeBuilder
{
    
    @Override
    public SqlNode build(Configuration configuration, MappedMethod method)
    {
        List<SqlNode> contents = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append("DELETE FROM ");
        sb.append(method.getTable());
        contents.add(new TextSqlNode(sb.toString()));
        contents.add(new CriteriaSqlNodeBuilder().build(configuration, method));
        return new MixedSqlNode(contents);
    }
    
}
