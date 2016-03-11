package com.codefarm.mybatis.orm.keygen;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.defaults.DefaultSqlSession.StrictMap;

public class SequenceKeyGenerator implements KeyGenerator
{
    private String squenceName;
    
    public SequenceKeyGenerator(String squenceName)
    {
        super();
        this.squenceName = squenceName;
    }
    
    @Override
    public void processAfter(Executor arg0, MappedStatement arg1,
            Statement arg2, Object arg3)
    {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void processBefore(Executor executor, MappedStatement ms,
            Statement arg2, Object parameter)
    {
        MetaObject newMetaObject = ms.getConfiguration()
                .newMetaObject(parameter);
        String[] keyProperties = ms.getKeyProperties();
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
                                populateKey(
                                        ms.getConfiguration()
                                                .newMetaObject(object),
                                        keyProperties,
                                        executor);
                                
                            }
                    }
                    else if (key.equals("array"))
                    {
                        
                        Object[] array = (Object[]) map.get(key);
                        if (array != null)
                            for (Object object : array)
                            {
                                populateKey(
                                        ms.getConfiguration()
                                                .newMetaObject(object),
                                        keyProperties,
                                        executor);
                            }
                    }
                }
            }
            
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }
    
    public String getSquenceName()
    {
        return squenceName;
    }
    
    public void setSquenceName(String squenceName)
    {
        this.squenceName = squenceName;
    }
    
    private void populateKey(MetaObject metaParam, String[] keyProperties,
            Executor executor) throws SQLException
    {
        Statement statement = executor.getTransaction()
                .getConnection()
                .createStatement();
        
        for (int i = 0; i < keyProperties.length; i++)
        {
            if (metaParam.getValue(keyProperties[i]) == null)
            {
                ResultSet rs = statement.executeQuery(
                        "select " + getSquenceName() + ".nextval from dual");
                rs.next();
                Long key = rs.getLong(1);
                metaParam.setValue(keyProperties[i], key);
                rs.close();
                
            }
            
        }
        statement.close();
    }
}
