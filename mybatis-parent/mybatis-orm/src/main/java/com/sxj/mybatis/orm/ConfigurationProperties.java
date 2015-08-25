package com.sxj.mybatis.orm;

import org.apache.ibatis.session.Configuration;

import com.sxj.mybatis.dialect.Dialect;
import com.sxj.mybatis.dialect.MySql5Dialect;
import com.sxj.mybatis.dialect.OracleDialect;

public class ConfigurationProperties
{
    public static Dialect getDialect(Configuration configuration)
    {
        Dialect.Type databaseType = null;
        try
        {
            String dbType = configuration.getVariables()
                    .getProperty("dialect")
                    .toUpperCase();
            databaseType = Dialect.Type.valueOf(dbType);
        }
        catch (Exception e)
        {
            throw new RuntimeException(
                    "the value of the dialect property in configuration.xml is not defined : "
                            + configuration.getVariables()
                                    .getProperty("dialect"));
        }
        Dialect dialect = null;
        switch (databaseType)
        {
            case MYSQL:
                dialect = new MySql5Dialect();
                break;
            case ORACLE:
                dialect = new OracleDialect();
                break;
        
        }
        return dialect;
    }
    
    public static boolean isSharded(Configuration configuration)
    {
        String property = configuration.getVariables().getProperty("sharded");
        if (property != null && property.trim().length() > 0)
            return Boolean.valueOf(property);
        return false;
    }
}
