package com.sxj.statemachine;

import com.sxj.statemachine.interfaces.EnterStateController;

/**
 * <p>Contains all information required for processing an event in a state machine. An event
 * is defined by an event identifier and an object -it might be null- that will be useful
 * when processing the transition (some kind of state).
 * 
 * <p>A non null object is returned during the {@link EnterStateController#execute(TransitionEvent event)}
 * if we want the state machine to process a new event before releasing the lock.
 * 
 * <p>This  is quite useful for some conditional states that might evaluate in runtime next event to be
 * consumed.
 */
public class EventInfo
{
    protected String event;
    
    protected Object object;
    
    public EventInfo(String event, Object object)
    {
        this.event = event;
        this.object = object;
    }
    
    /**
     * The event we want to consume after the phase is finished
     */
    public String getEvent()
    {
        return this.event;
    }
    
    /**
     * The object that we passed to the previous transition
     * @return
     */
    public Object getObject()
    {
        return this.object;
    }
}
