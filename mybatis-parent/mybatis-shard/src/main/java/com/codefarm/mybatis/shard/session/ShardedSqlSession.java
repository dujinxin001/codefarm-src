/*
 *    Copyright 2010-2011 The myBatis Team
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.codefarm.mybatis.shard.session;

import java.sql.PreparedStatement;

import javax.sql.DataSource;

import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.dao.support.PersistenceExceptionTranslator;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Handles MyBatis SqlSession life cycle. It can register and get SqlSessions
 * from Spring {@code TransactionSynchronizationManager}. Also works if no
 * transaction is active.
 * 
 * @version $Id: SqlSessionUtils.java 4198 2011-12-02 08:20:30Z
 *          eduardo.macarron@gmail.com $
 */
public final class ShardedSqlSession
{
    
    private static final Log logger = LogFactory.getLog(PreparedStatement.class);
    
    /**
     * This class can't be instantiated, exposes static utility methods only.
     */
    private ShardedSqlSession()
    {
        // do nothing
    }
    
    public static SqlSession getSqlSession(
            ShardSqlSessionFactory sessionFactory, ExecutorType executorType,
            PersistenceExceptionTranslator exceptionTranslator)
    {
        
        return SqlSessionUtils.getSqlSession(sessionFactory,
                executorType,
                exceptionTranslator);
        //return sess;
    }
    
    public static void closeSqlSession(SqlSession session, DataSource ds)
    {
        if (!TransactionSynchronizationManager.isSynchronizationActive())
        {
            session.close();
        }
    }
    
}
