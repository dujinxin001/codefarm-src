package com.sxj.mybatis.shard.session;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import javax.sql.DataSource;

import org.apache.ibatis.binding.BindingException;
import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.ExceptionUtil;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.spring.MyBatisExceptionTranslator;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.sxj.mybatis.orm.ConfigurationProperties;
import com.sxj.mybatis.orm.builder.GenericStatementBuilder;
import com.sxj.mybatis.orm.keygen.ShardJdbc4KeyGenerator;
import com.sxj.mybatis.orm.keygen.ShardKeyGenerator;
import com.sxj.mybatis.orm.keygen.ShardSnGenerator;
import com.sxj.mybatis.shard.MybatisConfiguration;
import com.sxj.mybatis.shard.datasource.DataSourceRouter;
import com.sxj.mybatis.shard.mapper.ShardMapperMethod;

public class ShardMapperProxy implements InvocationHandler, Serializable
{
    private static ThreadLocal<DataSource> currentDs = new ThreadLocal<DataSource>();
    
    private static final long serialVersionUID = -6424540398559729838L;
    
    //    private Configuration cfg = MybatisConfiguration.getConfiguration();
    
    private <T> ShardMapperProxy()
    {
    }
    
    public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable
    {
        if (method.getDeclaringClass() == Object.class)
        {
            return method.invoke(this, args);
        }
        final Class<?> declaringInterface = findDeclaringInterface(proxy,
                method);
        final ShardMapperMethod mapperMethod = new ShardMapperMethod(
                declaringInterface, method);
        String commandName = mapperMethod.getCommandName();
        MappedStatement ms = MybatisConfiguration.getConfiguration()
                .getMappedStatement(commandName);
        Object param = wrapCollection(mapperMethod.getParam(args));
        /**
         * 生成主键和SN，REQUIRE_NEW策略
         */
        processBefore(ms, param);
        
        // 根据该值进行路由，获取指定数据源的SqlSession
        SqlSession sqlSession = null;
        BoundSql boundSql = ms.getBoundSql(param);
        DataSource ds = DataSourceRouter.getDataSource(ms, boundSql, param);
        currentDs.set(ds);
        Object result = null;
        //        sqlSession = SqlSessionUtils.getSqlSession(ShardSqlSessionFactory.instance());
        //        if (ms.isUseCache())
        sqlSession = ShardedSqlSession.getSqlSession(ShardSqlSessionFactory.instance(),
                ExecutorType.REUSE,
                null);
        //        sqlSession = ShardedSqlSession.getSqlSession(ShardSqlSessionFactory.instance(),
        //                ExecutorType.SIMPLE,
        //                null);
        
        try
        {
            result = mapperMethod.execute(args, sqlSession);
            if (result == null && method.getReturnType().isPrimitive()
                    && !method.getReturnType().equals(Void.TYPE))
            {
                throw new BindingException(
                        "Mapper method '"
                                + method.getName()
                                + "' ("
                                + method.getDeclaringClass()
                                + ") attempted to return null from a method with a primitive return type ("
                                + method.getReturnType() + ").");
            }
            // 如果没有开启spring事务控制，则直接提交
            if (!TransactionSynchronizationManager.isSynchronizationActive())
            {
                sqlSession.commit(true);
            }
        }
        catch (Throwable t)
        {
            Throwable unwrapped = ExceptionUtil.unwrapThrowable(t);
            MyBatisExceptionTranslator exceptionTranslator = new MyBatisExceptionTranslator(
                    ds, true);
            if (exceptionTranslator != null
                    && unwrapped instanceof PersistenceException)
            {
                Throwable translated = exceptionTranslator.translateExceptionIfPossible((PersistenceException) unwrapped);
                if (translated != null)
                {
                    unwrapped = translated;
                }
            }
            throw unwrapped;
        }
        finally
        {
            //            ShardedSqlSession.closeSqlSession(sqlSession, ds);
        }
        return result;
    }
    
    private void processBefore(MappedStatement ms, Object param)
            throws SQLException
    {
        ShardKeyGenerator shardKeyGenerator = GenericStatementBuilder.getShardedKeyGenerators()
                .get(ms.getId());
        ShardSnGenerator shardSnGenerator = GenericStatementBuilder.getShardSnGenerators()
                .get(ms.getId());
        if (shardSnGenerator != null)
        {
            List<DataSource> snGeneratorDataSources = DataSourceRouter.getSnGeneratorDataSources();
            shardSnGenerator.process(snGeneratorDataSources.get(0),
                    param,
                    ConfigurationProperties.getDialect(MybatisConfiguration.getConfiguration()));
        }
        
        if (shardKeyGenerator != null)
        {
            if (shardKeyGenerator instanceof ShardJdbc4KeyGenerator)
            {
                List<DataSource> keyGeneratorDataSources = DataSourceRouter.getKeyGeneratorDataSources();
                int m = keyGeneratorDataSources.size();
                int n = (int) (keyGeneratorDataSources.size() * Math.random());
                ShardJdbc4KeyGenerator generator = ((ShardJdbc4KeyGenerator) shardKeyGenerator);
                generator.setStart(n + 1);
                generator.setStep(m);
                generator.process(keyGeneratorDataSources.get(n),
                        param,
                        ConfigurationProperties.getDialect(MybatisConfiguration.getConfiguration()));
            }
            else
                shardKeyGenerator.process(DataSourceRouter.getKeyGeneratorDataSource(),
                        param,
                        ConfigurationProperties.getDialect(MybatisConfiguration.getConfiguration()));
        }
    }
    
    private Class<?> findDeclaringInterface(Object proxy, Method method)
    {
        Class<?> declaringInterface = null;
        for (Class<?> iface : proxy.getClass().getInterfaces())
        {
            try
            {
                Method m = iface.getMethod(method.getName(),
                        method.getParameterTypes());
                if (declaringInterface != null)
                {
                    throw new BindingException(
                            "Ambiguous method mapping.  Two mapper interfaces contain the identical method signature for "
                                    + method);
                }
                else if (m != null)
                {
                    declaringInterface = iface;
                }
            }
            catch (Exception e)
            {
                // Intentionally ignore.
                // This is using exceptions for flow control,
                // but it's definitely faster.
            }
        }
        if (declaringInterface == null)
        {
            throw new BindingException(
                    "Could not find interface with the given method " + method);
        }
        return declaringInterface;
    }
    
    @SuppressWarnings("unchecked")
    public static <T> T newMapperProxy(Class<T> mapperInterface)
    {
        ClassLoader classLoader = mapperInterface.getClassLoader();
        Class<?>[] interfaces = new Class[] { mapperInterface };
        ShardMapperProxy proxy = new ShardMapperProxy();
        return (T) Proxy.newProxyInstance(classLoader, interfaces, proxy);
    }
    
    private Object wrapCollection(final Object object)
    {
        if (object instanceof List)
        {
            return new HashMap<String, Object>()
            {
                private static final long serialVersionUID = -4705063447161282548L;
                {
                    put("list", object);
                }
            };
        }
        else if (object != null && object.getClass().isArray())
        {
            return new HashMap<String, Object>()
            {
                private static final long serialVersionUID = 5771791745814754233L;
                {
                    put("array", object);
                }
            };
        }
        return object;
    }
    
    public static DataSource getCurrentDs()
    {
        return currentDs.get();
    }
    
}
