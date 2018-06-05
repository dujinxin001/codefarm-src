package com.codefarm.statemachine.strategy;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.LoggerFactory;

import com.codefarm.statemachine.EventInfo;
import com.codefarm.statemachine.StateMachineDefinitionImpl;
import com.codefarm.statemachine.StateMachineImpl;
import com.codefarm.statemachine.TransitionInfo;
import com.codefarm.statemachine.exceptions.StateMachineException;
import com.codefarm.statemachine.interfaces.EnterStateController;
import com.codefarm.statemachine.interfaces.ExitStateController;
import com.codefarm.statemachine.interfaces.StateMachineStrategy;
import com.codefarm.statemachine.interfaces.TransitionController;

/**
 * Single-thread implementation which user can configure whether it allows reentrant 
 * transitions
 */
public class ReentrantStrategy<S extends Enum<?>> implements
        StateMachineStrategy<S>
{
    private static org.slf4j.Logger l = LoggerFactory.getLogger(ReentrantStrategy.class);
    
    private ReentrantLock lock = new ReentrantLock();
    
    private boolean allowsReentrantTransitions;
    
    private boolean inTransition = false;
    
    /**
     * By default, we don't allow reentrant transitions. That means that if there
     * is a running transition and the developer, by mistake, tries to push
     * another transition from the same thread out of the allowed flow, it will
     * throw an exception
     */
    public ReentrantStrategy()
    {
        this(false);
    }
    
    protected ReentrantStrategy(boolean allowsReentrant)
    {
        this.allowsReentrantTransitions = allowsReentrant;
    }
    
    public void processEvent(StateMachineImpl<S> statemachine, String event,
            Object object) throws StateMachineException
    {
        StateMachineDefinitionImpl<S> stateMachineDefinition = (StateMachineDefinitionImpl<S>) statemachine.getDefinition();
        if (!stateMachineDefinition.isEvent(event))
            throw new StateMachineException("Event " + event + " not defined");
        
        try
        {
            // More fair approach when locking resources than
            // the normal tryLock one
            lock.tryLock(0, TimeUnit.SECONDS);
            
            if (!allowsReentrantTransitions)
            {
                if (inTransition)
                {
                    throw new StateMachineException(
                            "Reentrance from the same thread is not allowed");
                }
                else
                {
                    inTransition = true;
                }
            }
            
            S source = statemachine.getCurrentState();
            S target = stateMachineDefinition.getTargetState(source.name(),
                    event);
            TransitionInfo tEvent = new TransitionInfo(source.name(), event,
                    target.name(), object);
            
            ExitStateController exitController = stateMachineDefinition.getExitStateController(source.name());
            EnterStateController enterController = stateMachineDefinition.getEnterStateController(target.name());
            TransitionController transitionController = stateMachineDefinition.getTransitionController(source.name(),
                    event);
            
            if (exitController != null)
            {
                if (!exitController.execute(tEvent))
                {
                    l.debug("The controller cancelled the event propagation");
                    return;
                }
            }
            
            if (transitionController != null)
            {
                transitionController.execute(tEvent);
            }
            statemachine.setCurrentState(target);
            EventInfo result = null;
            if (enterController != null)
            {
                result = enterController.execute(tEvent);
            }
            
            if (result != null)
            {
                l.debug("#processEvent: Redirecting forced by controller to event "
                        + result.getEvent());
                inTransition = false;
                
                this.processEvent(statemachine,
                        result.getEvent(),
                        result.getObject());
            }
        }
        catch (InterruptedException ie)
        {
            l.warn("#processEvent: interrupted exception might not happen");
            throw new StateMachineException(ie);
        }
        finally
        {
            inTransition = false;
            lock.unlock();
        }
    }
}
