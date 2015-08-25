package com.sxj.mybatis.orm.keygen;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.sql.DataSource;

import com.sxj.mybatis.dialect.Dialect;
import com.sxj.mybatis.orm.annotations.Id;
import com.sxj.spring.modules.util.AnnotationUtils;
import com.sxj.spring.modules.util.Identities;
import com.sxj.spring.modules.util.Reflections;

public class ShardUuidKeyGenerator implements ShardKeyGenerator
{
    private int length;
    
    @Override
    public void process(DataSource ds, Object parameter, Dialect dialect)
            throws SQLException
    {
        if (parameter instanceof HashMap)
        {
            HashMap map = (HashMap) parameter;
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
                            populateKey(object);
                            
                        }
                }
                else if (key.equals("array"))
                {
                    
                    Object[] array = (Object[]) map.get(key);
                    if (array != null)
                        for (Object object : array)
                        {
                            populateKey(object);
                        }
                }
            }
        }
        else
        {
            populateKey(parameter);
        }
    }
    
    public ShardUuidKeyGenerator(int length)
    {
        super();
        this.length = length;
    }
    
    private void populateKey(Object object)
    {
        Field idField = AnnotationUtils.findDeclaredFieldWithAnnoation(Id.class,
                object.getClass());
        Reflections.setFieldValue(object,
                idField.getName(),
                Identities.randomBase62(length));
    }
    
    public int getLength()
    {
        return length;
    }
    
    public void setLength(int length)
    {
        this.length = length;
    }
    
}
