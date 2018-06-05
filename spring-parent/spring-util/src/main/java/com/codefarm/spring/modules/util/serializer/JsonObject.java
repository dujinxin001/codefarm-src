package com.codefarm.spring.modules.util.serializer;

public class JsonObject<T>
{
    private Class<T> type;
    
    private String value;
    
    public Class<T> getType()
    {
        return type;
    }
    
    public void setType(Class<T> type)
    
    {
        this.type = type;
    }
    
    public String getValue()
    {
        return value;
    }
    
    public void setValue(String value)
    {
        this.value = value;
    }
    
}
