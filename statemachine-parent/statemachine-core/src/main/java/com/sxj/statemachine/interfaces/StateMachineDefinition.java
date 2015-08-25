package com.sxj.statemachine.interfaces;

import java.util.List;
import java.util.Set;

import com.sxj.statemachine.exceptions.StateMachineException;

public interface StateMachineDefinition<S extends Enum<?>>
{
    /**
     * Is it an already define state?
     */
    public boolean isState(S state);
    
    /**
     * Is the one and only state?
     */
    public boolean isStartState(S state);
    
    /**
     * Is an Final state?
     */
    public boolean isFinalState(S state);
    
    /**
     * Returns a copy of the list of states
     */
    public List<S> getStates();
    
    /**
     * Returns the list of states that have been marked as Final ones
     */
    public List<S> getFinalStates();
    
    /**
     * Returns the start state of the state machine. There can only be one
     */
    public S getStartState();
    
    /**
     * Is it an already defined event?
     */
    public boolean isEvent(String event);
    
    /**
     * Returns a copy of the list of all the events
     */
    public Set<String> getEvents();
    
    /**
     * Returns a copy of all the events that could be applied to
     * <code>state</code>
     */
    public List<String> getApplicableEvents(String state);
    
    /**
     * Returns the state we reach for the specified source state and event
     */
    public S getTargetState(String source, String event)
            throws StateMachineException;
    
    public void setType(Class<S> type);
}
