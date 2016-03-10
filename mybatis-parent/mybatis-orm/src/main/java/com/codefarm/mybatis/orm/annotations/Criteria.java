package com.codefarm.mybatis.orm.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.codefarm.mybatis.orm.enums.Operator;

/**
 * @author zhangjian
 * 查询参数
 *
 */
@Target({ ElementType.PARAMETER, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Criteria
{
    String column();
    
    Operator operator() default Operator.EQUAL;
}
