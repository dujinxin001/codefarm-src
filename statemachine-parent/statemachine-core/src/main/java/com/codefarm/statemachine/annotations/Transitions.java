package com.codefarm.statemachine.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that allows a method to be marked with a list of 
 * {@link Transition} annotations. So, it means this method will
 * be executed for any transition on the list.
 * 
 * <p>
 * One common example is the noop method (eg. when we want to define
 * the transition but we don't want to execute any logic associated
 * to it)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
public @interface Transitions
{
    Transition[] value();
}
