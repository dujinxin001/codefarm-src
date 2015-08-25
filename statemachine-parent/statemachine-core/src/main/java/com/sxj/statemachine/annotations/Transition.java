package com.sxj.statemachine.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
/**
 * Defines a transition enter state phase. Methods annotated with it
 * might have the same contract as the {@link TransitionController}
 */
public @interface Transition
{
    /**
     * The state which we came from
     */
    String source();
    
    /**
     * The state which we are going to
     */
    String target();
    
    /**
     * The event that provokes the transition
     */
    String event();
    
    String callee() default "";
}
