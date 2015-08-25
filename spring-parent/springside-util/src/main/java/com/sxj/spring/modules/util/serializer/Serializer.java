package com.sxj.spring.modules.util.serializer;

public interface Serializer
{
    public String serialize(final Object obj);
    
    public Object deserialize(final String str);
}
