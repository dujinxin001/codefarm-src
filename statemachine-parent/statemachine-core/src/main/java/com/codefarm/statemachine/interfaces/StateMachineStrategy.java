package com.codefarm.statemachine.interfaces;

import com.codefarm.statemachine.StateMachineImpl;
import com.codefarm.statemachine.exceptions.StateMachineException;

public interface StateMachineStrategy<S extends Enum<?>>
{
    public void processEvent(StateMachineImpl<S> statemachine, String event,
            Object object) throws StateMachineException;
}
