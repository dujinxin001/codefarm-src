package com.codefarm.statemachine;

import com.codefarm.statemachine.TransitionInfo;
import com.codefarm.statemachine.annotations.StateMachine;
import com.codefarm.statemachine.annotations.Transition;
import com.codefarm.statemachine.annotations.Transitions;

@StateMachine(stateType = DemoStates.class, startState = "A", finalStates = {
        "C", "D" }, name = "eqwe")
@Transitions({ @Transition(source = "A", event = "AtoB", target = "B") })
public class StateMachineConfig
{
    
    @Transitions({ @Transition(source = "B", event = "BtoC", target = "C"),
            @Transition(source = "B", target = "B", event = "B2B") })
    public void noop(TransitionInfo event)
    {
        System.out.println("tx@:" + event.getEvent());
    }
}
