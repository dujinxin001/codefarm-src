package com.sxj.statemachine.spring;

import com.sxj.statemachine.TransitionInfo;
import com.sxj.statemachine.annotations.StateMachine;
import com.sxj.statemachine.annotations.Transition;
import com.sxj.statemachine.annotations.Transitions;

@StateMachine(name = "fsm2", stateType = State2.class, startState = "A2", finalStates = { "D2" })
public class FSM2
{
    @Transitions({ @Transition(source = "A2", target = "B2", event = "A2TOB2"),
            @Transition(source = "B2", target = "C2", event = "B2TOC2"),
            @Transition(source = "C2", target = "D2", event = "C2TOD2") })
    public void noop(TransitionInfo event)
    {
        System.out.println("tx@:" + event.getEvent());
    }
    
    @Transitions({ @Transition(source = "D2", target = "A2", event = "D2TOA2") })
    public void error(TransitionInfo event)
    {
        
    }
}
