package com.sxj.mybatis.dialect;

public class IDCfg
{
    private String table;
    
    private String idColumn;
    
    private String delimiterColumn;
    
    private String delimiterValue;
    
    private long currentIdValue;
    
    public String getTable()
    {
        return table;
    }
    
    public void setTable(String table)
    {
        this.table = table;
    }
    
    public String getIdColumn()
    {
        return idColumn;
    }
    
    public void setIdColumn(String idColumn)
    {
        this.idColumn = idColumn;
    }
    
    public String getDelimiterColumn()
    {
        return delimiterColumn;
    }
    
    public IDCfg(String table, String idColumn, String delimiterColumn)
    {
        super();
        this.table = table;
        this.idColumn = idColumn;
        this.delimiterColumn = delimiterColumn;
    }
    
    public void setDelimiterColumn(String delimiterColumn)
    {
        this.delimiterColumn = delimiterColumn;
    }
    
    public long getCurrentIdValue()
    {
        return currentIdValue;
    }
    
    public void setCurrentIdValue(long currentIdValue)
    {
        this.currentIdValue = currentIdValue;
    }
    
    public String getDelimiterValue()
    {
        return delimiterValue;
    }
    
    public void setDelimiterValue(String delimiterValue)
    {
        this.delimiterValue = delimiterValue;
    }
    
}
