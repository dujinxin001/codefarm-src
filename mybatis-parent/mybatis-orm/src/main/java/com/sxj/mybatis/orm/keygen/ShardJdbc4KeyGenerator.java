package com.sxj.mybatis.orm.keygen;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import javax.sql.DataSource;

import org.apache.ibatis.session.defaults.DefaultSqlSession.StrictMap;

import com.sxj.mybatis.dialect.Dialect;
import com.sxj.mybatis.dialect.IDCfg;
import com.sxj.mybatis.orm.annotations.GeneratedValue;
import com.sxj.mybatis.orm.annotations.Id;
import com.sxj.mybatis.orm.annotations.Table;
import com.sxj.spring.modules.util.AnnotationUtils;
import com.sxj.spring.modules.util.Reflections;

public class ShardJdbc4KeyGenerator implements ShardKeyGenerator
{
    private static Map<String, Field> cachedIdFields = new WeakHashMap<String, Field>();
    
    private ThreadLocal<Integer> start = new ThreadLocal<Integer>();
    
    private ThreadLocal<Integer> step = new ThreadLocal<Integer>();
    
    @Override
    public void process(DataSource ds, Object parameter, Dialect dialect)
            throws SQLException
    {
        Connection connection = ds.getConnection();
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
                                
                                processObject(connection, object, dialect);
                            }
                    }
                    else if (key.equals("array"))
                    {
                        
                        Object[] array = (Object[]) map.get(key);
                        if (array != null)
                            for (Object object : array)
                            {
                                processObject(connection, object, dialect);
                            }
                    }
                }
            }
            else
            {
                processObject(connection, parameter, dialect);
            }
        }
        catch (SQLException e)
        {
            connection.rollback();
        }
        //        finally
        //        {
        //            connection.close();
        //        }
    }
    
    private void processObject(Connection connection, Object parameter,
            Dialect dialect) throws SQLException
    {
        PreparedStatement prepareStatement = null;
        try
        {
            Class<?> userClass = Reflections.getUserClass(parameter);
            Field idField = findId(userClass);
            GeneratedValue generatedValue = idField.getAnnotation(GeneratedValue.class);
            String tableName = userClass.getAnnotation(Table.class).name();
            IDCfg idCfg = new IDCfg(generatedValue.table(),
                    generatedValue.idColumn(), generatedValue.delimiterColumn());
            idCfg.setDelimiterValue(tableName);
            initId(dialect, connection, idCfg);
            String idIncrSQL = dialect.getIdIncrSQL(idCfg);
            prepareStatement = connection.prepareStatement(idIncrSQL);
            prepareStatement.setInt(1, step.get());
            prepareStatement.setString(2, tableName);
            prepareStatement.setLong(3, idCfg.getCurrentIdValue());
            while (prepareStatement.executeUpdate() < 1)
            {
                prepareStatement.clearParameters();
                idCfg.setCurrentIdValue(idCfg.getCurrentIdValue() + step.get());
                prepareStatement.setInt(1, step.get());
                prepareStatement.setString(2, tableName);
                prepareStatement.setLong(3, idCfg.getCurrentIdValue());
            }
            prepareStatement.close();
            Reflections.setFieldValue(parameter,
                    idField.getName(),
                    idCfg.getCurrentIdValue());
            
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
            if (prepareStatement != null)
                prepareStatement.close();
        }
    }
    
    private void initId(Dialect dialect, Connection connection, IDCfg idCfg)
            throws SQLException
    {
        String idSelectSql = dialect.getIdSelectSQL(idCfg);
        PreparedStatement prepareStatement = null;
        ResultSet rs = null;
        try
        {
            prepareStatement = connection.prepareStatement(idSelectSql);
            prepareStatement.setString(1, idCfg.getDelimiterValue());
            rs = prepareStatement.executeQuery();
            rs.last();
            int row = rs.getRow();
            if (row == 0)
            {
                idCfg.setCurrentIdValue(start.get());
                String idInitSql = dialect.getIdInitSQL(idCfg);
                prepareStatement = connection.prepareStatement(idInitSql);
                prepareStatement.setInt(1, start.get());
                prepareStatement.setString(2, idCfg.getDelimiterValue());
                prepareStatement.executeUpdate();
            }
            else
            {
                idCfg.setCurrentIdValue(rs.getLong(1));
                
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (rs != null)
                rs.close();
            if (prepareStatement != null)
                prepareStatement.close();
        }
    }
    
    private Field findId(Class<?> userClass)
    {
        Field idField = cachedIdFields.get(userClass.getName());
        if (idField == null)
        {
            idField = AnnotationUtils.findDeclaredFieldWithAnnoation(Id.class,
                    userClass);
            cachedIdFields.put(userClass.getName(), idField);
        }
        return idField;
    }
    
    public void setStart(int start)
    {
        this.start.set(start);
        ;
    }
    
    public void setStep(int step)
    {
        this.step.set(step);
        ;
    }
    
}
