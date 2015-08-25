package com.sxj.statemachine;

import com.sxj.statemachine.annotations.StateMachine;
import com.sxj.statemachine.annotations.Transition;
import com.sxj.statemachine.annotations.Transitions;

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
