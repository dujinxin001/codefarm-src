package com.sxj.statemachine.spring;

import org.springframework.beans.factory.annotation.Autowired;

import com.sxj.statemachine.StateMachineImpl;
import com.sxj.statemachine.TransitionInfo;
import com.sxj.statemachine.annotations.StateMachine;
import com.sxj.statemachine.annotations.Transition;
import com.sxj.statemachine.annotations.Transitions;
import com.sxj.statemachine.exceptions.StateMachineException;

@StateMachine(name = "fsm1", stateType = State1.class, startState = "A1", finalStates = { "C1,D1" })
public class FSM1
{
    @Autowired
    private StateMachineImpl<State2> fsm2;
    
    @Autowired
    private FSMServiceImpl fsmServiceImpl;
    
    @Transitions({ @Transition(source = "A1", target = "B1", event = "A1TOB1"),
            @Transition(source = "B1", target = "C1", event = "B1TOC1"),
            @Transition(source = "B1", target = "D1", event = "B1TOD1") })
    public void noop(TransitionInfo event) throws StateMachineException
    {
        System.out.println("tx@:" + event.getEvent());
        fsm2.setCurrentState(State2.A2);
        fsm2.fire("A2TOB2", null);
        fsmServiceImpl.doSomething();
    }
    
    @Transitions({ @Transition(source = "C1", target = "A1", event = "C1TOA1") })
    public void error(TransitionInfo event)
    {
        
    }
}
