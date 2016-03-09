package com.codefarm.mybatis.pagination.interceptor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.executor.ExecutorException;
import org.apache.ibatis.executor.statement.BaseStatementHandler;
import org.apache.ibatis.executor.statement.RoutingStatementHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.ParameterMode;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.apache.ibatis.reflection.property.PropertyTokenizer;
import org.apache.ibatis.reflection.wrapper.DefaultObjectWrapperFactory;
import org.apache.ibatis.scripting.xmltags.ForEachSqlNode;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;

import com.codefarm.mybatis.dialect.Dialect;
import com.codefarm.mybatis.dialect.MySql5Dialect;
import com.codefarm.mybatis.dialect.OracleDialect;
import com.codefarm.mybatis.pagination.IPagable;
import com.codefarm.mybatis.pagination.Pagable;
import com.codefarm.spring.modules.util.Reflections;

@Intercepts({
        @Signature(type = StatementHandler.class, method = "prepare", args = {
                Connection.class }) })
public class PaginationInterceptor implements Interceptor
{
    
    private final static Log log = LogFactory
            .getLog(PaginationInterceptor.class);
    
    @Override
    public Object intercept(Invocation invocation) throws Throwable
    {
        
        RoutingStatementHandler statementHandler = (RoutingStatementHandler) invocation
                .getTarget();
        BaseStatementHandler delegate = (BaseStatementHandler) Reflections
                .getFieldValue(statementHandler, "delegate");
        MappedStatement mappedStatement = (MappedStatement) Reflections
                .getFieldValue(delegate, "mappedStatement");
        
        BoundSql boundSql = statementHandler.getBoundSql();
        Object parameter = boundSql.getParameterObject();
        if (parameter != null && parameter instanceof Pagable
                && ((IPagable) parameter).isPagable())
        {
            
            // if (page == null || !(page instanceof Page))
            // throw new Exception("分页函数参数只能是Page类型！");
            // 计算总行数
            Connection connection = (Connection) invocation.getArgs()[0];
            String sql = boundSql.getSql();
            Dialect dialect = getDialect(statementHandler);
            //            String countSql = "select count(0) from (" + sql + ") t";
            String countSql = dialect.getCountSQL(sql);
            PreparedStatement countStmt = connection.prepareStatement(countSql);
            BoundSql countBS = new BoundSql(mappedStatement.getConfiguration(),
                    countSql, boundSql.getParameterMappings(), parameter);
            setParameters(countStmt, mappedStatement, countBS, parameter);
            ResultSet rs = countStmt.executeQuery();
            int count = 0;
            if (rs.next())
            {
                count = rs.getInt(1);
            }
            rs.close();
            countStmt.close();
            
            IPagable page = (IPagable) parameter;
            page.setTotalResult(count);
            page.setTotalPage(page.getTotalResult() / page.getShowCount()
                    + (page.getTotalResult() % page.getShowCount() > 0 ? 1
                            : 0));
            
            String pageSql = dialect.getLimitString(sql,
                    (page.getCurrentPage() - 1) * page.getShowCount(),
                    page.getShowCount());
            //            ReflectHelper.setValueByFieldName(boundSql, "sql", pageSql); 
            Reflections.setFieldValue(boundSql, "sql", pageSql);// 将分页sql语句反射回BoundSql.
            log.debug("分页sql：" + boundSql.getSql());
        }
        return invocation.proceed();
    }
    
    private Dialect getDialect(RoutingStatementHandler statementHandler)
    {
        MetaObject metaStatementHandler = MetaObject.forObject(statementHandler,
                new DefaultObjectFactory(),
                new DefaultObjectWrapperFactory(),
                new DefaultReflectorFactory());
        Configuration configuration = (Configuration) metaStatementHandler
                .getValue("delegate.configuration");
        Dialect.Type databaseType = null;
        try
        {
            databaseType = Dialect.Type.valueOf(configuration.getVariables()
                    .getProperty("dialect")
                    .toUpperCase());
        }
        catch (Exception e)
        {
            throw new RuntimeException(
                    "the value of the dialect property in configuration.xml is not defined : "
                            + configuration.getVariables()
                                    .getProperty("dialect"));
        }
        Dialect dialect = null;
        switch (databaseType)
        {
            case MYSQL:
                dialect = new MySql5Dialect();
                break;
            case ORACLE:
                dialect = new OracleDialect();
                break;
            
        }
        return dialect;
    }
    
    private void setParameters(PreparedStatement ps,
            MappedStatement mappedStatement, BoundSql boundSql,
            Object parameterObject) throws SQLException
    {
        ErrorContext.instance().activity("setting parameters").object(
                mappedStatement.getParameterMap().getId());
        List<ParameterMapping> parameterMappings = boundSql
                .getParameterMappings();
        parameterMappings = mappedStatement.getBoundSql(parameterObject)
                .getParameterMappings();
        if (parameterMappings != null)
        {
            Configuration configuration = mappedStatement.getConfiguration();
            TypeHandlerRegistry typeHandlerRegistry = configuration
                    .getTypeHandlerRegistry();
            MetaObject metaObject = parameterObject == null ? null
                    : configuration.newMetaObject(parameterObject);
            for (int i = 0; i < parameterMappings.size(); i++)
            {
                ParameterMapping parameterMapping = parameterMappings.get(i);
                if (parameterMapping.getMode() != ParameterMode.OUT)
                {
                    Object value;
                    String propertyName = parameterMapping.getProperty();
                    PropertyTokenizer prop = new PropertyTokenizer(
                            propertyName);
                    if (parameterObject == null)
                    {
                        value = null;
                    }
                    else if (typeHandlerRegistry
                            .hasTypeHandler(parameterObject.getClass()))
                    {
                        value = parameterObject;
                    }
                    else if (boundSql.hasAdditionalParameter(propertyName))
                    {
                        value = boundSql.getAdditionalParameter(propertyName);
                    }
                    else if (propertyName.startsWith(ForEachSqlNode.ITEM_PREFIX)
                            && boundSql.hasAdditionalParameter(prop.getName()))
                    {
                        value = boundSql.getAdditionalParameter(prop.getName());
                        if (value != null)
                        {
                            value = configuration.newMetaObject(value)
                                    .getValue(propertyName.substring(
                                            prop.getName().length()));
                        }
                    }
                    else
                    {
                        value = metaObject == null ? null
                                : metaObject.getValue(propertyName);
                    }
                    TypeHandler typeHandler = parameterMapping.getTypeHandler();
                    if (typeHandler == null)
                    {
                        throw new ExecutorException(
                                "There was no TypeHandler found for parameter "
                                        + propertyName + " of statement "
                                        + mappedStatement.getId());
                    }
                    typeHandler.setParameter(ps,
                            i + 1,
                            value,
                            parameterMapping.getJdbcType());
                }
            }
        }
    }
    
    @Override
    public Object plugin(Object target)
    {
        return Plugin.wrap(target, this);
    }
    
    @Override
    public void setProperties(Properties properties)
    {
    }
    
}
