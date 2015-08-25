package com.sxj.mybatis.orm.keygen;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.defaults.DefaultSqlSession.StrictMap;

import com.sxj.mybatis.dialect.Dialect;
import com.sxj.mybatis.dialect.SNCfg;
import com.sxj.mybatis.orm.ConfigurationProperties;
import com.sxj.mybatis.orm.annotations.Sn;
import com.sxj.spring.modules.util.ReflectUtils;
import com.sxj.spring.modules.util.Reflections;

public class SnGenerator implements KeyGenerator
{
    private static Map<String, List<Field>> cachedSnFields = new WeakHashMap<String, List<Field>>();
    
    private void generateSn(Connection connection, Object parameter,
            Dialect dialect) throws SQLException
    {
        Statement statement = null;
        try
        {
            statement = connection.createStatement();
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
                                
                                process(statement, object, dialect);
                            }
                    }
                    else if (key.equals("array"))
                    {
                        
                        Object[] array = (Object[]) map.get(key);
                        if (array != null)
                            for (Object object : array)
                            {
                                process(statement, object, dialect);
                            }
                    }
                }
            }
            else
            {
                process(statement, parameter, dialect);
            }
            //connection.commit();
        }
        catch (SQLException e)
        {
            throw e;
        }
        finally
        {
            if (statement != null)
                statement.close();
            //            connection.close();
            //            if (connection != null)
            //                connection.close();
        }
        
    }
    
    private void process(Statement statement, Object parameter, Dialect dialect)
    {
        try
        {
            Class<?> userClass = Reflections.getUserClass(parameter);
            List<Field> snFields = findSnFields(userClass);
            for (Field field : snFields)
            {
                Sn sn = field.getAnnotation(Sn.class);
                SNCfg snPojo = new SNCfg();
                snPojo.setStep(sn.step());
                snPojo.setSn(sn.sn());
                snPojo.setStub(sn.stub());
                String stubValueProperty = sn.stubValueProperty();
                if (stubValueProperty != null
                        && stubValueProperty.trim().length() > 0)
                {
                    String value = (String) Reflections.invokeGetter(parameter,
                            stubValueProperty);
                    snPojo.setStubValue(value);
                }
                else
                    snPojo.setStubValue(sn.stubValue());
                snPojo.setTableName(sn.table());
                if (parameter instanceof SnStub)
                {
                    snPojo.setStubValue((String) Reflections.invokeGetter(parameter,
                            "stubValue"));
                }
                initSn(dialect, statement, snPojo);
                String snSql = dialect.getSnIncrSQL(snPojo);
                
                while (statement.executeUpdate(snSql) < 1)
                {
                    snPojo.setCurrent(snPojo.getCurrent() + snPojo.getStep());
                    snSql = dialect.getSnIncrSQL(snPojo);
                }
                if (StringUtils.isEmpty(sn.pattern()))
                    Reflections.invokeSetter(parameter,
                            field.getName(),
                            (snPojo.getCurrent() + snPojo.getStep()));
                else
                {
                    DecimalFormat df = new DecimalFormat(sn.pattern());
                    if (sn.appendStubValue())
                        Reflections.invokeSetter(parameter,
                                field.getName(),
                                snPojo.getStubValue()
                                        + df.format(snPojo.getCurrent()
                                                + snPojo.getStep()));
                    else
                        Reflections.invokeSetter(parameter,
                                field.getName(),
                                df.format(snPojo.getCurrent()
                                        + snPojo.getStep()));
                }
                
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
    
    private void initSn(Dialect dialect, Statement statement, SNCfg snPojo)
            throws SQLException
    {
        String snSelectString = dialect.getSnSelectSQL(snPojo);
        ResultSet rs = null;
        try
        {
            rs = statement.executeQuery(snSelectString);
            rs.last();
            int row = rs.getRow();
            if (row == 0)
            {
                String snInsertString = dialect.getSnInitSQL(snPojo);
                statement.executeUpdate(snInsertString);
                snPojo.setCurrent(0);
            }
            else
                snPojo.setCurrent(rs.getLong(1));
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (rs != null)
                rs.close();
        }
    }
    
    private List<Field> findSnFields(Class<?> userClass)
    {
        List<Field> snFields = cachedSnFields.get(userClass.getName());
        if (snFields == null)
        {
            snFields = ReflectUtils.findFieldsAnnotatedWith(userClass, Sn.class);
            cachedSnFields.put(userClass.getName(), snFields);
        }
        return snFields;
    }
    
    @Override
    public void processBefore(Executor executor, MappedStatement ms,
            Statement stmt, Object parameter)
    {
        try
        {
            generateSn(executor.getTransaction().getConnection(),
                    parameter,
                    ConfigurationProperties.getDialect(ms.getConfiguration()));
        }
        catch (SQLException sqle)
        {
            throw new RuntimeException(sqle);
        }
    }
    
    @Override
    public void processAfter(Executor executor, MappedStatement ms,
            Statement stmt, Object parameter)
    {
        
    }
    
    public static void main(String... strings)
    {
        DecimalFormat df = new DecimalFormat("");
        System.out.println(df.format(1));
    }
    
}
