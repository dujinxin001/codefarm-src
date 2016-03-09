package com.codefarm.mybatis.dialect;

import java.util.HashMap;
import java.util.Map;

public abstract class Dialect
{
    private static Map<String, String> cachedSqls = new HashMap<String, String>();
    
    private Type type;
    
    public static enum Type
    {
        MYSQL, ORACLE
    }
    
    public abstract String getLimitString(String sql, int skipResults,
            int maxResults);
    
    public abstract String getCountSQL(String sql);
    
    public abstract String getSnIncrSQL(SNCfg sn);
    
    public abstract String getSnInitSQL(SNCfg sn);
    
    public abstract String getSnSelectSQL(SNCfg sn);
    
    public abstract String getIdSelectSQL(IDCfg cfg);
    
    public abstract String getIdInitSQL(IDCfg cfg);
    
    public abstract String getIdIncrSQL(IDCfg cfg);
    
    public Type getType()
    {
        return type;
    }
    
    public void setType(Type type)
    {
        this.type = type;
    }
    
    protected void cache(String key, String sql)
    {
        if (!cachedSqls.containsKey(key))
            cachedSqls.put(key, sql);
    }
    
    protected String getCache(String key)
    {
        return cachedSqls.get(key);
    }
    
}
