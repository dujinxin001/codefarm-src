package com.codefarm.jsonrpc.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target(TYPE)
@Retention(RUNTIME)
public @interface JsonRpcService
{
    
    /**
     * The path that the service is available at.
     * @return the path
     */
    String value();
    
    /**
     * Whether or not to use named parameters.
     * @return
     */
    boolean useNamedParams() default false;
    
}
