package com.codefarm.mybatis.orm.keygen;

import org.apache.ibatis.reflection.MetaObject;

import com.codefarm.spring.modules.util.Reflections;

public class LongSequenceKeyGenerator extends AbstractSequenceKeyGenerator
{
    
    public LongSequenceKeyGenerator(String squenceName)
    {
        super(squenceName);
    }
    
    @Override
    protected void setValue(MetaObject meta, String keyProperty, Long key)
    {
        meta.setValue(keyProperty, key);
    }
    
    @Override
    protected void setValue(Object param, String keyProperty, Long key)
    {
        Reflections.setFieldValue(param, keyProperty, key);
    }
    
}
