package com.codefarm.statemachine;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codefarm.statemachine.annotations.OnEnter;
import com.codefarm.statemachine.annotations.OnExit;
import com.codefarm.statemachine.annotations.Transition;
import com.codefarm.statemachine.exceptions.StateMachineException;
import com.codefarm.statemachine.interfaces.EnterStateController;
import com.codefarm.statemachine.interfaces.ExitStateController;
import com.codefarm.statemachine.interfaces.StateMachineDefinition;
import com.codefarm.statemachine.interfaces.TransitionController;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class StateMachineDefinitionImpl<S extends Enum<?>>
        implements StateMachineDefinition<S>
{
    
    private Class<S> genericType;
    
    private static Logger logger = LoggerFactory
            .getLogger(StateMachineDefinitionImpl.class);
            
    private S startState;
    
    private HashMap<String, State> states;
    
    private HashSet<String> events;
    
    public StateMachineDefinitionImpl()
    {
        this.states = Maps.newHashMap();
        this.events = Sets.newHashSet();
    }
    
    public boolean isEvent(String event)
    {
        return this.events.contains(event);
    }
    
    public boolean isState(S state)
    {
        return this.states.containsKey(state.name());
    }
    
    public boolean isStartState(S state)
    {
        boolean result = false;
        
        State s = states.get(state);
        if (s != null)
            result = s.isStart();
            
        return result;
    }
    
    public boolean isFinalState(S state)
    {
        boolean result = false;
        State s = states.get(state);
        if (s != null)
            result = s.isFinal();
            
        return result;
    }
    
    public void defineEvent(String event) throws StateMachineException
    {
        checkEventNotNull(event);
        
        if (events.contains(event))
            throw new StateMachineException(
                    "Event " + event + " already defined in the state machine");
                    
        events.add(event);
        logger.debug("#defineEvent succeed for event id " + event);
    }
    
    public Set<String> getEvents()
    {
        return Collections.unmodifiableSet(events);
    }
    
    public void defineState(S state) throws StateMachineException
    {
        this.defineState(state, false, false);
    }
    
    public void defineState(S state, boolean isStart, boolean isFinal)
            throws StateMachineException
    {
        
        checkStateNotNull(state);
        
        if (isStart && startState != null)
            throw new StateMachineException(
                    "A state machine can only have one start state."
                            + " Cannot define state " + state
                            + " as start state because " + startState
                            + " was already defined as the one and only");
                            
        if (isStart && isFinal)
            throw new StateMachineException("Cannot define state " + state
                    + " as start and end. It does not make sense");
                    
        if (states.containsKey(state))
        {
            throw new StateMachineException(
                    "State " + state + " already defined");
        }
        else
        {
            states.put(state.name(), new State(state.name(), isStart, isFinal));
        }
        
        logger.debug("#defineState succeed for state id " + state);
        
        if (isStart)
            this.startState = state;
    }
    
    public S getStartState()
    {
        return this.startState;
    }
    
    public List<S> getFinalStates()
    {
        ArrayList<S> result = new ArrayList<S>();
        for (State state : this.states.values())
        {
            if (state.isFinal)
            {
                result.add(getActualState(state.getName()));
            }
        }
        return result;
    }
    
    protected S getActualState(String name)
    {
        S[] enumConstants = genericType.getEnumConstants();
        for (S s : enumConstants)
            if (s.name().equals(name))
                return s;
        return null;
    }
    
    private State checkStateExists(String state) throws StateMachineException
    {
        if (!isState(getActualState(state)))
            throw new StateMachineException(
                    "State " + state + " does not exist");
                    
        return states.get(state);
    }
    
    private void checkStateNotNull(S state)
    {
        if (state == null)
            throw new IllegalArgumentException(
                    "Can not define a state with null value");
    }
    
    private void checkEventExists(String event) throws StateMachineException
    {
        if (!isEvent(event))
            throw new StateMachineException(
                    "Event " + event + " does not exist");
    }
    
    private void checkEventNotNull(String event)
    {
        if (event == null)
            throw new IllegalArgumentException(
                    "Can not define an event with null value");
    }
    
    void defineTransition(Transition transition, final Method method,
            final Object callee) throws StateMachineException
    {
        if (method != null)
            this.defineTransition(transition.source(),
                    transition.event(),
                    transition.target(),
                    new TransitionController()
                    {
                        public void execute(TransitionInfo event)
                        {
                            try
                            {
                                method.invoke(callee, event);
                            }
                            catch (IllegalAccessException e)
                            {
                                logger.error("This should never happen");
                                throw new RuntimeException(e);
                            }
                            catch (IllegalArgumentException e)
                            {
                                logger.error("This should never happen");
                                throw new RuntimeException(e);
                            }
                            catch (InvocationTargetException swallow)
                            {
                                logger.error(
                                        "Exceptions should be treated in the controller. Swallowing it",
                                        swallow);
                                throw new RuntimeException(swallow);
                            }
                        }
                    });
        else
            this.defineTransition(transition.source(),
                    transition.event(),
                    transition.target(),
                    new TransitionController()
                    {
                        public void execute(TransitionInfo event)
                        {
                            // System.out.println("txdddd@:" +
                            // event.getEvent());
                            logger.debug(event.toString());
                        }
                    });
    }
    
    public void defineTransition(String source, String event, String target,
            TransitionController controller) throws StateMachineException
    {
        
        State sourceState = checkStateExists(source);
        checkStateExists(target);
        checkEventExists(event);
        
        if (sourceState.isFinal() && !source.equals(target))
            throw new StateMachineException(
                    "Cannot create transitions from the final state " + source);
        sourceState.setTransitionController(event, target, controller);
    }
    
    void defineExitState(OnExit ann, final Method method, final Object callee)
            throws StateMachineException
    {
        this.defineExitState(ann.value(), new ExitStateController()
        {
            public Boolean execute(TransitionInfo event)
            {
                Boolean result = null;
                try
                {
                    result = (Boolean) method.invoke(callee, event);
                    if (result == null)
                        result = true; // We might be able to define void
                                       // methods
                }
                catch (IllegalAccessException e)
                {
                    logger.error("This should never happen");
                }
                catch (IllegalArgumentException e)
                {
                    logger.error("This should never happen");
                }
                catch (InvocationTargetException swallow)
                {
                    logger.error(
                            "Exceptions should be treated in the controller. Swallowing it",
                            swallow);
                }
                return result;
            }
        });
    }
    
    public void defineExitState(String state, ExitStateController controller)
            throws StateMachineException
    {
        State internalState = checkStateExists(state);
        internalState.setExitStateController(controller);
    }
    
    void defineEnterState(final OnEnter ann, final Method method,
            final Object callee) throws StateMachineException
    {
        this.defineEnterState(ann.value(), new EnterStateController()
        {
            public EventInfo execute(TransitionInfo event)
            {
                EventInfo evtInfo = null;
                try
                {
                    evtInfo = (EventInfo) method.invoke(callee, event);
                }
                catch (IllegalAccessException e)
                {
                    logger.error("This should never happen");
                }
                catch (IllegalArgumentException e)
                {
                    logger.error("This should never happen");
                }
                catch (InvocationTargetException swallow)
                {
                    logger.error(
                            "Exceptions should be treated in the controller. Swallowing it",
                            swallow);
                }
                return evtInfo;
            }
        });
    }
    
    public void defineEnterState(String state, EnterStateController controller)
            throws StateMachineException
    {
        State internalState = checkStateExists(state);
        internalState.setEnterStateController(controller);
    }
    
    public TransitionController getTransitionController(String state,
            String event) throws StateMachineException
    {
        TransitionController controller = null;
        State internalState = checkStateExists(state);
        if (internalState != null)
            controller = internalState.getTransitionController(event);
            
        return controller;
    }
    
    public EnterStateController getEnterStateController(String state)
            throws StateMachineException
    {
        EnterStateController controller = null;
        State internalState = checkStateExists(state);
        if (internalState != null)
            controller = internalState.getEnterStateController();
            
        return controller;
    }
    
    public ExitStateController getExitStateController(String state)
            throws StateMachineException
    {
        ExitStateController controller = null;
        State internalState = checkStateExists(state);
        if (internalState != null)
            controller = internalState.getExitStateController();
            
        return controller;
    }
    
    /**
     * This method is only invoked for valid source states, so no additional
     * checks are required.
     * 
     * @throws TransitionNotDefinedException
     *             in case the transition does not exist
     */
    public S getTargetState(String source, String event)
            throws StateMachineException
    {
        State src = checkStateExists(source);
        
        HashMap<String, TransitionTarget> txs = src.getTransitions();
        TransitionTarget target = txs.get(event);
        if (target == null)
            throw new StateMachineException("Transition from state " + source
                    + " with event " + event + " not defined");
                    
        return getActualState(target.getState());
    }
    
    public List<S> getStates()
    {
        ArrayList<S> result = new ArrayList<S>();
        for (String key : states.keySet())
            result.add(getActualState(key));
        return result;
    }
    
    public List<String> getApplicableEvents(String source)
    {
        List<String> result = new ArrayList<String>();
        
        if (this.isState(getActualState(source)))
        {
            HashMap<String, TransitionTarget> transitions = states.get(source)
                    .getTransitions();
            for (String key : transitions.keySet())
                result.add(key);
        }
        
        return result;
    }
    
    private void printTransitionsForState(State state, StringBuilder sb)
    {
        String NEWLINE = "\n";
        sb.append("<Transitions>").append(NEWLINE);
        
        if (state.getExitStateController() != null)
            sb.append("<ExitState state=\"")
                    .append(state.getName())
                    .append("\" />")
                    .append(NEWLINE);
                    
        HashMap<String, TransitionTarget> txs = state.getTransitions();
        for (String event : txs.keySet())
        {
            TransitionTarget target = txs.get(event);
            sb.append("<Transition ")
                    .append("source=\"")
                    .append(state.getName())
                    .append("\" ")
                    .append("event=\"")
                    .append(event)
                    .append("\" ")
                    .append("target=\"")
                    .append(target.getState())
                    .append("\"")
                    .append(" />")
                    .append(NEWLINE);
        }
        
        if (state.getEnterStateController() != null)
            sb.append("<EnterState state=\"")
                    .append(state.getName())
                    .append("\" />");
                    
        sb.append("</Transitions>").append(NEWLINE);
    }
    
    /**
     * Returns the state machine definition in a XML format. This is not a cheap
     * operation.
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        String NEWLINE = "\n";
        sb.append("<StateMachineDefinition");
        if (startState != null)
            sb.append(" startState=\"").append(startState).append("\"");
            
        sb.append(">").append(NEWLINE);
        
        sb.append("<States>").append(NEWLINE);
        for (State state : states.values())
        {
            if (state.isFinal())
            {
                sb.append("<FinalState>")
                        .append(state)
                        .append("</FinalState>")
                        .append(NEWLINE);
            }
            else
            {
                sb.append("<State>")
                        .append(state)
                        .append("</State>")
                        .append(NEWLINE);
            }
            printTransitionsForState(state, sb);
        }
        sb.append("</States>").append(NEWLINE);
        
        sb.append("<Events>").append(NEWLINE);
        for (String event : events)
        {
            sb.append("<Event>")
                    .append(event)
                    .append("</Event>")
                    .append(NEWLINE);
        }
        sb.append("</Events>").append(NEWLINE);
        
        sb.append("</StateMachineDefinition>");
        return sb.toString();
    }
    
    private class TransitionTarget
    {
        private String state;
        
        private TransitionController transitionController;
        
        public TransitionTarget(String state,
                TransitionController transitionController)
        {
            super();
            this.state = state;
            this.transitionController = transitionController;
        }
        
        public String getState()
        {
            return state;
        }
        
        public TransitionController getTransitionController()
        {
            return transitionController;
        }
    }
    
    /**
     * Contains all state related info. The name, whether the state is final or
     * not and the list of transitions to other states.
     */
    private class State
    {
        private String name;
        
        private boolean isStart;
        
        private boolean isFinal;
        
        private EnterStateController enterStateController;
        
        private ExitStateController exitStateController;
        
        private HashMap<String, TransitionTarget> transitions;
        
        public State(String name, boolean isStart, boolean isFinal)
        {
            this.name = name;
            this.isStart = isStart;
            this.isFinal = isFinal;
            this.transitions = new HashMap<String, TransitionTarget>();
        }
        
        public String getName()
        {
            return this.name;
        }
        
        public boolean isStart()
        {
            return this.isStart;
        }
        
        public boolean isFinal()
        {
            return this.isFinal;
        }
        
        public void setEnterStateController(
                EnterStateController enterStateController)
        {
            this.enterStateController = enterStateController;
        }
        
        public EnterStateController getEnterStateController()
        {
            return this.enterStateController;
        }
        
        public ExitStateController getExitStateController()
        {
            return exitStateController;
        }
        
        public void setExitStateController(
                ExitStateController exitStateController)
        {
            this.exitStateController = exitStateController;
        }
        
        public void setTransitionController(String event, String target,
                TransitionController controller)
        {
            if (!transitions.containsKey(event))
                transitions.put(event,
                        new TransitionTarget(target, controller));
        }
        
        public TransitionController getTransitionController(String event)
        {
            TransitionController controller = null;
            TransitionTarget info = this.transitions.get(event);
            if (info != null)
                controller = info.getTransitionController();
                
            return controller;
        }
        
        public HashMap<String, TransitionTarget> getTransitions()
        {
            return this.transitions;
        }
        
        public String toString()
        {
            return name;
        }
    }
    
    public void setType(Class<S> type)
    {
        genericType = type;
    }
}
