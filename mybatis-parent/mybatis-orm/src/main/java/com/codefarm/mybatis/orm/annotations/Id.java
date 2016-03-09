package com.codefarm.mybatis.orm.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Id
{
    
    /**
     * true：keygenerator会覆盖设置的值，false：使用赋值
     * @return
     */
    boolean generatedKeys() default true;
    
    String column() default "";
}
