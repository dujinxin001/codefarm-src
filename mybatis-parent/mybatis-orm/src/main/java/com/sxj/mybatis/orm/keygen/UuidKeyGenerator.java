package com.sxj.mybatis.orm.keygen;

import java.sql.Statement;
import java.util.Iterator;
import java.util.List;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.defaults.DefaultSqlSession.StrictMap;

import com.sxj.mybatis.orm.builder.GenericStatementBuilder;
import com.sxj.spring.modules.util.Identities;

public class UuidKeyGenerator implements KeyGenerator
{
    private int length;
    
    public UuidKeyGenerator(int length)
    {
        super();
        this.length = length;
    }
    
    public void setLength(int length)
    {
        this.length = length;
    }
    
    @Override
    public void processBefore(Executor executor, MappedStatement ms,
            Statement stmt, Object parameter)
    {
        MetaObject newMetaObject = ms.getConfiguration()
                .newMetaObject(parameter);
        String[] keyProperties = ms.getKeyProperties();
        SnGenerator snGenerator = GenericStatementBuilder.getSnGenerators()
                .get(ms.getId());
        try
        {
            if (parameter instanceof StrictMap)
            {
                StrictMap map = (StrictMap) parameter;
                Iterator<String> keySet = map.keySet().iterator();
                while (keySet.hasNext())
                {
                    String key = keySet.next();
                    if (key.equals("list"))
                    {
                        List list = (List) map.get(key);
                        if (list != null)
                            for (Object object : list)
                            {
                                populateKey(ms.getConfiguration()
                                        .newMetaObject(object), keyProperties);
                                
                            }
                    }
                    else if (key.equals("array"))
                    {
                        
                        Object[] array = (Object[]) map.get(key);
                        if (array != null)
                            for (Object object : array)
                            {
                                populateKey(ms.getConfiguration()
                                        .newMetaObject(object), keyProperties);
                            }
                    }
                }
            }
            else
            {
                populateKey(newMetaObject, keyProperties);
            }
            if (snGenerator != null)
                snGenerator.processBefore(executor, ms, stmt, parameter);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public void processAfter(Executor executor, MappedStatement ms,
            Statement stmt, Object parameter)
    {
    }
    
    private void populateKey(MetaObject metaParam, String[] keyProperties)
    {
        for (int i = 0; i < keyProperties.length; i++)
        {
            if (metaParam.getValue(keyProperties[i]) == null)
                metaParam.setValue(keyProperties[i],
                        Identities.randomBase62(length));
        }
    }
    
}
