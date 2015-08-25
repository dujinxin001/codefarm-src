package com.sxj.mybatis.shard.transaction;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.sxj.mybatis.shard.datasource.DataSourceFactory;
import com.sxj.mybatis.shard.datasource.DataSourceFactory.DataSourceNode;

public class ShardManagedTransactionManager implements
        PlatformTransactionManager, InitializingBean
{
    private final Logger log = LoggerFactory.getLogger(getClass());
    
    private Map<DataSource, DataSourceTransactionManager> transactionManagers = new HashMap<DataSource, DataSourceTransactionManager>();
    
    private static final ThreadLocal<Boolean> isReadOnly = new ThreadLocal<Boolean>();
    
    /**
     * 统计提交
     */
    private AtomicInteger commitCount = new AtomicInteger(0);
    
    /**
     * 统计回滚
     */
    private AtomicInteger rollbackCount = new AtomicInteger(0);
    
    @Override
    public void afterPropertiesSet() throws Exception
    {
        List<DataSourceNode> dataNodes = DataSourceFactory.getDataNodes();
        for (DataSourceNode node : dataNodes)
        {
            List<DataSource> writeNodes = node.getWriteNodes();
            for (DataSource write : writeNodes)
            {
                transactionManagers.put(write,
                        new DataSourceTransactionManager(write));
            }
        }
        // TODO Auto-generated method stub
        addShutdownHook();
    }
    
    private void addShutdownHook()
    {
        Runtime.getRuntime().addShutdownHook(new Thread()
        {
            @SuppressWarnings("static-access")
            @Override
            public void run()
            {
                // rollback 和 commit如果不为0
                while (commitCount.get() != 0)
                {
                    log.info("Waiting for commit transaction.");
                    try
                    {
                        Thread.currentThread().sleep(1);
                    }
                    catch (InterruptedException e)
                    {
                        log.warn("interrupted when shuting down the query executor:\n{}",
                                e);
                    }
                }
                while (rollbackCount.get() != 0)
                {
                    log.info("Waiting for rollback transaction.");
                    try
                    {
                        Thread.currentThread().sleep(1);
                    }
                    catch (InterruptedException e)
                    {
                        log.warn("interrupted when shuting down the query executor:\n{}",
                                e);
                    }
                }
                log.info("Transaction success.");
            }
        });
        
    }
    
    @Override
    public TransactionStatus getTransaction(TransactionDefinition definition)
            throws TransactionException
    {
        ShardManagedTransactionStatus transactionStatus = new ShardManagedTransactionStatus();
        isReadOnly.set(definition.isReadOnly());
        log.debug("Operation '" + definition.getName()
                + "' starting transaction.");
        List<DataSourceNode> dataNodes = DataSourceFactory.getDataNodes();
        for (DataSourceNode node : dataNodes)
        {
            List<DataSource> dataSources = node.getWriteNodes();
            for (DataSource dataSource : dataSources)
            {
                DefaultTransactionDefinition defaultTransactionDefinition = new DefaultTransactionDefinition(
                        definition);
                defaultTransactionDefinition.setName(definition.getName());
                
                PlatformTransactionManager txManager = this.transactionManagers.get(dataSource);
                TransactionStatus status = txManager.getTransaction(defaultTransactionDefinition);
                TransactionSynchronizationManager.setCurrentTransactionName(defaultTransactionDefinition.getName());
                transactionStatus.put(dataSource, status);
            }
        }
        
        return transactionStatus;
    }
    
    @Override
    public void commit(TransactionStatus status) throws TransactionException
    {
        Throwable ex = null;
        //      Collections.reverse(dataSources);
        List<DataSourceNode> dataNodes = DataSourceFactory.getDataNodes();
        for (DataSourceNode node : dataNodes)
        {
            List<DataSource> dataSources = node.getWriteNodes();
            for (int i = dataSources.size() - 1; i >= 0; i--)
            {
                DataSource dataSource = dataSources.get(i);
                try
                {
                    commitCount.addAndGet(1);
                    
                    log.debug("Committing JDBC transaction");
                    
                    DataSourceTransactionManager txManager = this.transactionManagers.get(dataSource);
                    
                    DefaultTransactionStatus transactionStatus = (DefaultTransactionStatus) ((ShardManagedTransactionStatus) status).get(dataSource);
                    txManager.commit(transactionStatus);
                    log.debug("Commit JDBC transaction success");
                }
                catch (Throwable e)
                {
                    log.debug("Could not commit JDBC transaction", e);
                    ex = e;
                }
                finally
                {
                    commitCount.addAndGet(-1);
                }
            }
            
            if (ex != null)
            {
                throw new RuntimeException(ex);
            }
        }
        
    }
    
    @Override
    public void rollback(TransactionStatus status) throws TransactionException
    {
        Throwable ex = null;
        
        //Cannot deactivate transaction synchronization - not active
        //      Collections.reverse(dataSources);
        List<DataSourceNode> dataNodes = DataSourceFactory.getDataNodes();
        for (DataSourceNode node : dataNodes)
        {
            List<DataSource> dataSources = node.getWriteNodes();
            for (int i = dataSources.size() - 1; i >= 0; i--)
            {
                DataSource dataSource = dataSources.get(i);
                try
                {
                    log.debug("Rolling back JDBC transaction");
                    rollbackCount.addAndGet(1);
                    DataSourceTransactionManager txManager = this.transactionManagers.get(dataSource);
                    TransactionStatus currentStatus = ((ShardManagedTransactionStatus) status).get(dataSource);
                    txManager.rollback(currentStatus);
                    log.info("Roll back JDBC transaction success");
                }
                catch (Throwable e)
                {
                    log.info("Could not roll back JDBC transaction", e);
                    ex = e;
                }
                finally
                {
                    rollbackCount.addAndGet(-1);
                }
            }
            
            if (ex != null)
            {
                throw new RuntimeException(ex);
            }
        }
        
    }
    
    public static boolean isCurrentTransactionReadOnly()
    {
        if (isReadOnly.get() == null)
            return true;
        return isReadOnly.get();
    }
    
    //    public static void putConnection(DataSource ds, Connection conn)
    //    {
    //        Map<DataSource, Connection> conns = currentConnections.get();
    //        if (conns == null)
    //        {
    //            conns = new HashMap<DataSource, Connection>();
    //            currentConnections.set(conns);
    //        }
    //        conns.put(ds, conn);
    //    }
    
    public static Connection getConnection(DataSource ds) throws SQLException
    {
        //        Map<DataSource, Connection> conns = currentConnections.get();
        //        if (conns == null)
        //        {
        Connection connection = DataSourceUtils.getConnection(ds);
        return connection;
        //        }
        //        
        //        return conns.get(ds);
    }
    
}
