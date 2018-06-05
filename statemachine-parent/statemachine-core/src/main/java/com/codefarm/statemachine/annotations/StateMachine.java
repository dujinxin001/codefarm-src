package com.codefarm.statemachine.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
/**
 * All state machine classes must be annotated with it. Just to have some discipline about them :-)
 */
public @interface StateMachine {
	Class<? extends Enum<?>> stateType();

	String startState();

	String[] finalStates();

	String name();
}
