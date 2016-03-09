package com.codefarm.mybatis.shard.session;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.ibatis.exceptions.ExceptionFactory;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.TransactionIsolationLevel;
import org.apache.ibatis.session.defaults.DefaultSqlSession;
import org.apache.ibatis.transaction.Transaction;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.managed.ManagedTransactionFactory;

import com.codefarm.mybatis.shard.MybatisConfiguration;

public class ShardSqlSessionFactory implements SqlSessionFactory
{
    
    private Configuration configuration;
    
    private TransactionFactory managedTransactionFactory;
    
    private static ShardSqlSessionFactory factory;
    
    private ShardSqlSessionFactory()
    {
    }
    
    public static ShardSqlSessionFactory instance()
    {
        if (factory == null)
        {
            factory = new ShardSqlSessionFactory();
            factory.configuration = MybatisConfiguration.getConfiguration();
            factory.managedTransactionFactory = new ManagedTransactionFactory();
        }
        return factory;
    }
    
    //    public SqlSession openSession(DataSource ds)
    //    {
    //        return openSessionFromDataSource(ds,
    //                configuration.getDefaultExecutorType(),
    //                null,
    //                false);
    //    }
    
    public Configuration getConfiguration()
    {
        return configuration;
    }
    
    private SqlSession openSessionFromDataSource(ExecutorType execType,
            TransactionIsolationLevel level, boolean autoCommit)
    {
        Transaction tx = null;
        try
        {
            DataSource ds = ShardMapperProxy.getCurrentDs();
            final Environment environment = configuration.getEnvironment();
            final TransactionFactory transactionFactory = getTransactionFactoryFromEnvironment(environment);
            
            tx = transactionFactory.newTransaction(ds, level, autoCommit);
            
            final Executor executor = configuration.newExecutor(tx, execType);
            return new DefaultSqlSession(configuration, executor);
        }
        catch (Exception e)
        {
            closeTransaction(tx); // may have fetched a connection so lets call close()
            throw ExceptionFactory.wrapException("Error opening session.  Cause: "
                    + e,
                    e);
        }
        finally
        {
            ErrorContext.instance().reset();
        }
    }
    
    private TransactionFactory getTransactionFactoryFromEnvironment(
            Environment environment)
    {
        if (environment == null || environment.getTransactionFactory() == null)
        {
            return managedTransactionFactory;
        }
        return environment.getTransactionFactory();
    }
    
    private void closeTransaction(Transaction tx)
    {
        if (tx != null)
        {
            try
            {
                tx.close();
            }
            catch (SQLException ignore)
            {
                // Intentionally ignore. Prefer previous error.
            }
        }
    }
    
    public SqlSession openSession()
    {
        return openSessionFromDataSource(configuration.getDefaultExecutorType(),
                null,
                false);
    }
    
    public SqlSession openSession(boolean autoCommit)
    {
        return openSessionFromDataSource(configuration.getDefaultExecutorType(),
                null,
                autoCommit);
    }
    
    public SqlSession openSession(ExecutorType execType)
    {
        return openSessionFromDataSource(execType, null, false);
    }
    
    public SqlSession openSession(TransactionIsolationLevel level)
    {
        return openSessionFromDataSource(configuration.getDefaultExecutorType(),
                level,
                false);
    }
    
    public SqlSession openSession(ExecutorType execType,
            TransactionIsolationLevel level)
    {
        return openSessionFromDataSource(execType, level, false);
    }
    
    public SqlSession openSession(ExecutorType execType, boolean autoCommit)
    {
        return openSessionFromDataSource(execType, null, autoCommit);
    }
    
    public SqlSession openSession(Connection connection)
    {
        throw new RuntimeException("Not supported in Sharded");
    }
    
    public SqlSession openSession(ExecutorType execType, Connection connection)
    {
        throw new RuntimeException("Not supported in Sharded");
    }
    
}
