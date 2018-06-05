package com.codefarm.statemachine.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
/**
 * Defines a state in an annotated state machine. The annotated
 * field must be defined as <code>public static final</code>
 */
public @interface State
{
    /** Whether the state is the start one of the state machine */
    boolean isStart() default false;
    
    /** Whether the state is an end one of the state machine */
    boolean isFinal() default false;
}
