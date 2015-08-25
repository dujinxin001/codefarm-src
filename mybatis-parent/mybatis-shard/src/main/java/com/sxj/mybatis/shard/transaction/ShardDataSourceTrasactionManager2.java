package com.sxj.mybatis.shard.transaction;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSession;
import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.sxj.mybatis.shard.session.ShardedSqlSession;

public class ShardDataSourceTrasactionManager2 extends
        AbstractPlatformTransactionManager
{
    private static final ThreadLocal<Boolean> isReadOnly = new ThreadLocal<Boolean>();
    
    private DataSource dataSource;
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    private static ThreadLocal<Map<DataSource, SqlSession>> currentSessions = new ThreadLocal<Map<DataSource, SqlSession>>();
    
    private static ThreadLocal<Map<DataSource, Connection>> currentConnections = new ThreadLocal<Map<DataSource, Connection>>();
    
    public static void putSession(DataSource ds, SqlSession sqlSession)
    {
        
        Map<DataSource, SqlSession> sessions = currentSessions.get();
        if (sessions == null)
        {
            sessions = new HashMap<DataSource, SqlSession>();
            currentSessions.set(sessions);
        }
        sessions.put(ds, sqlSession);
    }
    
    public static SqlSession getSession(DataSource ds)
    {
        Map<DataSource, SqlSession> sessions = currentSessions.get();
        if (sessions == null)
        {
            return null;
        }
        return sessions.get(ds);
    }
    
    public static void putConnection(DataSource ds, Connection conn)
    {
        Map<DataSource, Connection> conns = currentConnections.get();
        if (conns == null)
        {
            conns = new HashMap<DataSource, Connection>();
            currentConnections.set(conns);
        }
        conns.put(ds, conn);
    }
    
    public static Connection getConnection(DataSource ds)
    {
        Map<DataSource, Connection> conns = currentConnections.get();
        if (conns == null)
        {
            return null;
        }
        return conns.get(ds);
    }
    
    public static void closeConnection(DataSource ds)
    {
        Map<DataSource, Connection> conns = currentConnections.get();
        if (conns == null)
        {
            return;
        }
        Connection conn = conns.get(ds);
        conns.remove(ds);
        try
        {
            conn.close();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }
    
    protected Object doGetTransaction() throws TransactionException
    {
        TransactionObject txObject = new TransactionObject();
        ConnectionHolder conHolder = (ConnectionHolder) TransactionSynchronizationManager.getResource(this.dataSource);
        txObject.setConnectionHolder(conHolder, false);
        return txObject;
    }
    
    protected void doBegin(Object transaction, TransactionDefinition definition)
            throws TransactionException
    {
        isReadOnly.set(definition.isReadOnly());
        //        System.out.println("Begin Transaction");
        logger.debug("Begin transaction with name [" + definition.getName()
                + "]: " + definition);
    }
    
    protected void doCommit(DefaultTransactionStatus status)
            throws TransactionException
    {
        
        doCommit();
        doCloseSession();
    }
    
    protected void doRollback(DefaultTransactionStatus status)
            throws TransactionException
    {
        
        doRollback();
        doCloseSession();
    }
    
    private void doCloseSession()
    {
        if (currentSessions.get() == null)
            return;
        for (Map.Entry<DataSource, SqlSession> entry : currentSessions.get()
                .entrySet())
        {
            ShardedSqlSession.closeSqlSession(entry.getValue(), entry.getKey());
        }
        currentSessions.remove();
    }
    
    private void doCommit()
    {
        Map<DataSource, Connection> conns = currentConnections.get();
        if (conns == null)
            return;
        for (Map.Entry<DataSource, Connection> entry : conns.entrySet())
        {
            try
            {
                entry.getValue().commit();
                entry.getValue().close();
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
        currentConnections.remove();
    }
    
    private void doRollback()
    {
        Map<DataSource, Connection> conns = currentConnections.get();
        if (conns == null)
            return;
        for (Map.Entry<DataSource, Connection> entry : conns.entrySet())
        {
            try
            {
                entry.getValue().rollback();
                entry.getValue().close();
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
        currentConnections.remove();
    }
    
    public static boolean isCurrentTransactionReadOnly()
    {
        if (isReadOnly.get() == null)
            return true;
        return isReadOnly.get();
    }
    
    public DataSource getDataSource()
    {
        return dataSource;
    }
    
    public void setDataSource(DataSource dataSource)
    {
        this.dataSource = dataSource;
    }
}
