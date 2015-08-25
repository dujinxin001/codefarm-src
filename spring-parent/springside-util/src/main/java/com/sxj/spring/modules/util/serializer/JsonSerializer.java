package com.sxj.spring.modules.util.serializer;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonSerializer implements Serializer
{
    
    @Override
    public String serialize(final Object obj)
    {
        try
        {
            ObjectMapper mapper = new ObjectMapper();
            String valueAsString = mapper.writeValueAsString(obj);
            JsonObject cacheObject = new JsonObject();
            cacheObject.setType(obj.getClass());
            cacheObject.setValue(valueAsString);
            return mapper.writeValueAsString(cacheObject);
        }
        catch (Exception e)
        {
            throw new SerializeException(e);
        }
    }
    
    @Override
    public Object deserialize(final String str)
    {
        if (str == null)
            return null;
        ObjectMapper mapper = new ObjectMapper();
        try
        {
            JsonObject readValue = mapper.readValue(str, JsonObject.class);
            return mapper.readValue(readValue.getValue(), readValue.getType());
        }
        catch (IOException e)
        {
            throw new SerializeException(e);
        }
    }
    
}
