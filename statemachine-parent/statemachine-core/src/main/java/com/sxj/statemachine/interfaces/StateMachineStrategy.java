package com.sxj.statemachine.interfaces;

import com.sxj.statemachine.StateMachineImpl;
import com.sxj.statemachine.exceptions.StateMachineException;

public interface StateMachineStrategy<S extends Enum<?>>
{
    public void processEvent(StateMachineImpl<S> statemachine, String event,
            Object object) throws StateMachineException;
}
