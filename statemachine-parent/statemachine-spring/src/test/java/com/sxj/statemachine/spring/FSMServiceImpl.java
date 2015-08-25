package com.sxj.statemachine.spring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.sxj.statemachine.StateMachineImpl;
import com.sxj.statemachine.exceptions.StateMachineException;

@Service
public class FSMServiceImpl {
	@Autowired
	@Qualifier("fsm2")
	private StateMachineImpl<State2> fsm2;

	public void doSomething() throws StateMachineException {
		fsm2.setCurrentState(State2.B2);
		fsm2.fire("B2TOC2", null);
		System.out.println("dosomething here!!!!!");
	}
}
