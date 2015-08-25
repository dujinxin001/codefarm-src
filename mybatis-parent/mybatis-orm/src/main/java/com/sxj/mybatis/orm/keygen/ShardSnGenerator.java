package com.sxj.mybatis.orm.keygen;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import javax.sql.DataSource;

import com.sxj.mybatis.dialect.Dialect;
import com.sxj.mybatis.dialect.SNCfg;
import com.sxj.mybatis.orm.annotations.Sn;
import com.sxj.spring.modules.util.ReflectUtils;
import com.sxj.spring.modules.util.Reflections;

public class ShardSnGenerator implements ShardKeyGenerator
{
    private static Map<String, List<Field>> cachedSnFields = new WeakHashMap<String, List<Field>>();
    
    @Override
    public void process(DataSource ds, Object parameter, Dialect dialect)
            throws SQLException
    {
        Connection connection = null;
        Statement statement = null;
        
        try
        {
            connection = ds.getConnection();
            statement = connection.createStatement();
            List list = new ArrayList();
            if (parameter instanceof Collection<?>)
            {
                Collection tmp = (Collection.class.cast(parameter));
                list.addAll(tmp);
            }
            else if (parameter.getClass().isArray())
            {
                list = Arrays.asList(parameter);
            }
            else
            {
                process(statement, parameter, dialect);
                return;
            }
            for (Object object : list)
                process(statement, object, dialect);
            //connection.commit();
        }
        catch (SQLException sqle)
        {
            throw sqle;
        }
        finally
        {
            if (statement != null)
                statement.close();
            
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
                snPojo.setStubValue(sn.stubValue());
                snPojo.setTableName(sn.table());
                if (parameter instanceof SnStub)
                {
                    snPojo.setStubValue((String) Reflections.invokeGetter(parameter,
                            "stubValue"));
                }
                String snSql = dialect.getSnIncrSQL(snPojo);
                
                initSn(dialect, statement, snPojo);
                
                while (statement.executeUpdate(snSql) < 1)
                {
                    snPojo.setCurrent(snPojo.getCurrent() + snPojo.getStep());
                }
                DecimalFormat df = new DecimalFormat(sn.pattern());
                Reflections.invokeSetter(parameter,
                        field.getName(),
                        snPojo.getStubValue()
                                + df.format(snPojo.getCurrent()
                                        + snPojo.getStep()));
                
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
    
}
