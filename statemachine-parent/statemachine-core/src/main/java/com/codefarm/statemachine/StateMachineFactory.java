package com.codefarm.statemachine;

import com.codefarm.statemachine.exceptions.StateMachineException;
import com.codefarm.statemachine.interfaces.IStateMachine;

public class StateMachineFactory
{
    static StateMachineBuilder<Enum<?>> builder = new StateMachineBuilder<Enum<?>>();
    
    public static IStateMachine<Enum<?>> newInstance(Object config)
            throws StateMachineException
    {
        return builder.newNonReentrant(config);
    }
    
    public static IStateMachine<Enum<?>> newInstance(Object config,
            Object enhanced) throws StateMachineException
    {
        return builder.newNonReentrant(config, enhanced);
    }
}
