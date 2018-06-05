package com.codefarm.statemachine.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
/**
 * Defines a transition enter state phase. Methods annotated with it
 * might have the same contract as the {@link EnterStateController}.
 * 
 * <p>There is only one difference with the contract: if the method returns
 * void, the state machine execution will assume we don't want to forward
 * to any other state.
 */
public @interface OnEnter
{
    String value();
}
