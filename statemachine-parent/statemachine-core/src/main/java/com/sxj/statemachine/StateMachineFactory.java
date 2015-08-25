package com.sxj.statemachine;

import com.sxj.statemachine.exceptions.StateMachineException;
import com.sxj.statemachine.interfaces.IStateMachine;

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
