package com.sxj.statemachine.interfaces;

import com.sxj.statemachine.EventInfo;
import com.sxj.statemachine.TransitionInfo;

/**
 * Corresponds to the enter state phase. During this phase, the state machine allows
 * us to force the state machine to process another event without releasing the lock, 
 * so we don't need to fight with other threads. 
 * 
 * <p>
 * The method returns a {@link EventInfo} object. If not null, the event that is inside the object 
 * is going to be executedby the state machine without releasing the lock. This is very useful in certain
 * circumstances (specially ghost-like condition states that we need to check a lot of
 * conditions for taking a decision about the next actions to happen)</li>
 */
public interface EnterStateController
{
    public EventInfo execute(TransitionInfo event);
}
