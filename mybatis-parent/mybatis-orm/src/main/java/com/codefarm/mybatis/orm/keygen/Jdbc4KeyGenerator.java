package com.codefarm.mybatis.orm.keygen;

import java.sql.Statement;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.keygen.Jdbc3KeyGenerator;
import org.apache.ibatis.mapping.MappedStatement;

import com.codefarm.mybatis.orm.builder.GenericStatementBuilder;

public class Jdbc4KeyGenerator extends Jdbc3KeyGenerator
{
    
    @Override
    public void processBefore(Executor executor, MappedStatement ms,
            Statement stmt, Object parameter)
    {
        super.processBefore(executor, ms, stmt, parameter);
        SnGenerator snGenerator = GenericStatementBuilder.getSnGenerators()
                .get(ms.getId());
        if (snGenerator != null)
            snGenerator.processBefore(executor, ms, stmt, parameter);
    }
    
}
