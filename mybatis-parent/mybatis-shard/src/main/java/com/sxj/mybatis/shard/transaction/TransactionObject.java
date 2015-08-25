package com.sxj.mybatis.shard.transaction;

import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.jdbc.datasource.JdbcTransactionObjectSupport;

public class TransactionObject extends JdbcTransactionObjectSupport
{
    private boolean newConnectionHolder;
    
    private boolean mustRestoreAutoCommit;
    
    public void setConnectionHolder(ConnectionHolder connectionHolder,
            boolean newConnectionHolder)
    {
        super.setConnectionHolder(connectionHolder);
        this.newConnectionHolder = newConnectionHolder;
    }
    
    public boolean isNewConnectionHolder()
    {
        return this.newConnectionHolder;
    }
    
    public boolean hasTransaction()
    {
        return false;
    }
    
    public void setMustRestoreAutoCommit(boolean mustRestoreAutoCommit)
    {
        this.mustRestoreAutoCommit = mustRestoreAutoCommit;
    }
    
    public boolean isMustRestoreAutoCommit()
    {
        return this.mustRestoreAutoCommit;
    }
    
    @Override
    public boolean isRollbackOnly()
    {
        // TODO Auto-generated method stub
        return false;
    }
    
}
