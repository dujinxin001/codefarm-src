package com.sxj.statemachine;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Test;

import com.sxj.statemachine.exceptions.StateMachineException;

public class StateMachineTest
{
    
    @AfterClass
    public static void tearDownAfterClass() throws Exception
    {
    }
    
    @After
    public void tearDown() throws Exception
    {
    }
    
    @Test
    public void test() throws StateMachineException
    {
        StateMachineImpl<DemoStates> machine = (StateMachineImpl<DemoStates>) new StateMachineBuilder<DemoStates>().newNonReentrant(new StateMachineConfig());
        //        machine.fire("AtoB", null);
        machine.setCurrentState(DemoStates.B);
        machine.fire("B2B", null);
        DemoStates currentState = machine.getCurrentState();
        System.out.println("==========" + currentState);
    }
    
}
