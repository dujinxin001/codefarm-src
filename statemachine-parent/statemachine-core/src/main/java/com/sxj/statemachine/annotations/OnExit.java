package com.sxj.statemachine.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
/**
 * Defines a transition enter state phase. Methods annotated with it
 * might have the same contract as the {@link ExitStateController}
 * 
 * <p>It allows defining the method as void, though. It means that
 * the phase will return a <code>true</code>, which means the transition
 * continues without problems.
 */
public @interface OnExit
{
    public String value();
}
