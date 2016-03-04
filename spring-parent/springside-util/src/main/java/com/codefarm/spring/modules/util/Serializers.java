package com.codefarm.spring.modules.util;

import com.codefarm.spring.modules.util.serializer.JdkSerializer;
import com.codefarm.spring.modules.util.serializer.JsonSerializer;
import com.codefarm.spring.modules.util.serializer.Serializer;

public class Serializers
{
    public static Serializer getJsonSerializer()
    {
        return new JsonSerializer();
    }
    
    public static Serializer getJdkSerializer()
    {
        return new JdkSerializer();
    }
}
