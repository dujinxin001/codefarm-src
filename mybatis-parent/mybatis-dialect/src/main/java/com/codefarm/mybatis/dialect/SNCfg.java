package com.codefarm.mybatis.dialect;

public class SNCfg
{
    private int step;
    
    private long current;
    
    private String tableName;
    
    private String stub;
    
    private String stubValue;
    
    private String stubValueProperty;
    
    private String sn;
    
    public String getTableName()
    {
        return tableName;
    }
    
    public final void setTableName(String tableName)
    {
        this.tableName = tableName;
    }
    
    public final String getStub()
    {
        return stub;
    }
    
    public final void setStub(String stub)
    {
        this.stub = stub;
    }
    
    public final int getStep()
    {
        return step;
    }
    
    public final void setStep(int step)
    {
        this.step = step;
    }
    
    public final String getStubValue()
    {
        return stubValue;
    }
    
    public void setStubValue(String stubValue)
    {
        this.stubValue = stubValue;
    }
    
    public final String getSn()
    {
        return sn;
    }
    
    public final void setSn(String sn)
    {
        this.sn = sn;
    }
    
    public final long getCurrent()
    {
        return current;
    }
    
    public final void setCurrent(long current)
    {
        this.current = current;
    }
    
    public String getStubValueProperty()
    {
        return stubValueProperty;
    }
    
    public void setStubValueProperty(String stubValueProperty)
    {
        this.stubValueProperty = stubValueProperty;
    }
    
}
