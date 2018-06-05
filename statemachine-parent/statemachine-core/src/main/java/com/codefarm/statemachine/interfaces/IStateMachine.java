package com.codefarm.statemachine.interfaces;

import com.codefarm.statemachine.exceptions.StateMachineException;

public interface IStateMachine<S extends Enum<?>>
{
    /**
     * Returns the current state of the state machine
     */
    public S getCurrentState();
    
    /**
     * Returns the state machine definition
     */
    public StateMachineDefinition<S> getDefinition();
    
    /**
     * Consumes an event following the selected strategy.
     */
    public void fire(String event, Object object) throws StateMachineException;
}
