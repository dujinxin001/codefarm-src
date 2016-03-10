package com.codefarm.mybatis.orm.keygen;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.mapping.MappedStatement;

import com.codefarm.mybatis.orm.annotations.Id;
import com.codefarm.spring.modules.util.AnnotationUtils;
import com.codefarm.spring.modules.util.Reflections;

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
    public void processBefore(Executor executor, MappedStatement arg1,
            Statement arg2, Object parameter)
    {
        try
        {
            Statement statement = executor.getTransaction()
                    .getConnection()
                    .createStatement();
            ResultSet rs = statement.executeQuery(
                    "select " + getSquenceName() + ".nextval from dual");
            rs.next();
            Long key = rs.getLong(1);
            Field idField = AnnotationUtils.findDeclaredFieldWithAnnoation(
                    Id.class, parameter.getClass());
            Reflections.invokeSetter(parameter, idField.getName(), key);
            rs.close();
            statement.close();
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
    
}
