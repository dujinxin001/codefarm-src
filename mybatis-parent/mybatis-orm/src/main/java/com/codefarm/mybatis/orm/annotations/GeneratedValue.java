package com.codefarm.mybatis.orm.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.executor.keygen.NoKeyGenerator;

/**
 * Class description goes here.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface GeneratedValue
{
    
    GenerationType strategy() default GenerationType.AUTO;
    
    int length() default 32;
    
    String table() default "IDENTITIES";
    
    String idColumn() default "IDENTITIES_ID";
    
    String delimiterColumn() default "IDENTITIES_DELIMITER";
    
    String sequence() default "KEY_SEQUENCE";
    
    Class<? extends KeyGenerator> generator() default NoKeyGenerator.class;
}
