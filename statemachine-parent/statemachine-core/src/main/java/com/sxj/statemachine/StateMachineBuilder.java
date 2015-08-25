package com.sxj.statemachine;

import static org.slf4j.LoggerFactory.getLogger;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;

import com.sxj.statemachine.annotations.Event;
import com.sxj.statemachine.annotations.OnEnter;
import com.sxj.statemachine.annotations.OnExit;
import com.sxj.statemachine.annotations.State;
import com.sxj.statemachine.annotations.StateMachine;
import com.sxj.statemachine.annotations.Transition;
import com.sxj.statemachine.annotations.Transitions;
import com.sxj.statemachine.exceptions.StateMachineException;
import com.sxj.statemachine.interfaces.IStateMachine;
import com.sxj.statemachine.interfaces.StateMachineDefinition;
import com.sxj.statemachine.strategy.NonReentrantStrategy;
import com.sxj.statemachine.strategy.ReentrantStrategy;

/**
 * Helper class for creating state machines from a state machine definition or
 * from an annotated class.
 * 
 * <p>
 * The annotated class must be annotated with {@link StateMachine}
 */
public class StateMachineBuilder<S extends Enum<?>>
{
    protected static Logger l = getLogger(StateMachineBuilder.class);
    
    private Object enhanced;
    
    public IStateMachine<S> newReentrant(StateMachineDefinition<S> definition)
            throws StateMachineException
    {
        return new StateMachineImpl<S>(definition, new ReentrantStrategy<S>());
    }
    
    public IStateMachine<S> newReentrant(Object instance)
            throws StateMachineException
    {
        return new StateMachineImpl<S>(processAnnotatedController(instance),
                new ReentrantStrategy<S>());
    }
    
    public IStateMachine<S> newNonReentrant(StateMachineDefinition<S> definition)
            throws StateMachineException
    {
        return new StateMachineImpl<S>(definition,
                new NonReentrantStrategy<S>());
    }
    
    public IStateMachine<S> newNonReentrant(Object instance, Object enhanced)
            throws StateMachineException
    {
        this.enhanced = enhanced;
        return new StateMachineImpl<S>(processAnnotatedController(instance),
                new NonReentrantStrategy<S>());
    }
    
    public IStateMachine<S> newNonReentrant(Object instance)
            throws StateMachineException
    {
        return new StateMachineImpl<S>(processAnnotatedController(instance),
                new NonReentrantStrategy<S>());
    }
    
    private boolean isFinal(String state, String... states)
    {
        for (String s : states)
        {
            if (s.equals(states))
                return true;
        }
        return false;
    }
    
    private void checkClassAnnotation(StateMachineDefinitionImpl<S> definition,
            Object instance) throws StateMachineException
    {
        Class<?> clazz = instance.getClass();
        if (!clazz.isAnnotationPresent(com.sxj.statemachine.annotations.StateMachine.class))
        {
            throw new StateMachineException(
                    "All state machines must be annotated with the @AStateMachine annotation");
        }
        StateMachine machine = clazz.getAnnotation(StateMachine.class);
        Class<S> stateType = (Class<S>) machine.stateType();
        definition.setType(stateType);
        String startState = machine.startState();
        String[] finalStates = machine.finalStates();
        List<S> asList = Arrays.asList(stateType.getEnumConstants());
        for (S e : asList)
        {
            if (e.name().equals(startState))
                definition.defineState(e, true, false);
            else if (isFinal(e.name(), finalStates))
                definition.defineState(e, false, true);
            else
                definition.defineState(e);
        }
        
        Transitions transitions = clazz.getAnnotation(Transitions.class);
        if (transitions != null)
        {
            Transition[] values = transitions.value();
            for (Transition t : values)
            {
                String methodName = t.callee();
                checkTransitionAnnotation(instance,
                        definition,
                        getMethod(methodName, clazz),
                        t);
            }
        }
    }
    
    private Method getMethod(String name, Class<?> clazz)
    {
        Method[] methods = clazz.getMethods();
        for (Method method : methods)
        {
            if (method.getName().equals(name))
                return method;
        }
        return null;
    }
    
    private StateMachineDefinitionImpl<S> checkFieldAnnotations(
            StateMachineDefinitionImpl<S> stateMachineDefinition,
            Object instance) throws StateMachineException
    {
        Class<?> clazz = instance.getClass();
        
        // Let's process the events and states first.
        // We look for the State, StartState and Event annotations
        for (Field field : clazz.getDeclaredFields())
        {
            if (field.isAnnotationPresent(State.class))
                checkStateAnnotation(instance,
                        stateMachineDefinition,
                        field,
                        field.getAnnotation(State.class));
            
            if (field.isAnnotationPresent(Event.class))
                checkEventAnnotation(instance,
                        stateMachineDefinition,
                        field,
                        field.getAnnotation(Event.class));
        }
        
        return stateMachineDefinition;
    }
    
    private StateMachineDefinition<S> processAnnotatedController(Object instance)
            throws StateMachineException
    {
        StateMachineDefinitionImpl<S> stateMachineDefinition = new StateMachineDefinitionImpl<S>();
        
        checkClassAnnotation(stateMachineDefinition, instance);
        checkFieldAnnotations(stateMachineDefinition, instance);
        checkTransitionAnnotations(stateMachineDefinition, instance);
        
        return stateMachineDefinition;
    }
    
    private void checkTransitionAnnotations(
            StateMachineDefinitionImpl<S> definition, Object instance)
            throws StateMachineException
    {
        // Let's process the transitions
        Class<?> clazz = instance.getClass();
        for (Method method : clazz.getMethods())
        {
            if (method.isAnnotationPresent(Transitions.class))
            {
                Transitions transitions = method.getAnnotation(Transitions.class);
                for (Transition transition : transitions.value())
                    checkTransitionAnnotation(instance,
                            definition,
                            method,
                            transition);
            }
            else if (method.isAnnotationPresent(Transition.class))
            {
                checkTransitionAnnotation(instance,
                        definition,
                        method,
                        method.getAnnotation(Transition.class));
            }
            else if (method.isAnnotationPresent(OnEnter.class))
            {
                checkEnterStateAnnotation(instance,
                        definition,
                        method,
                        method.getAnnotation(OnEnter.class));
            }
            else if (method.isAnnotationPresent(OnExit.class))
            {
                checkExitStateAnnotation(instance,
                        definition,
                        method,
                        method.getAnnotation(OnExit.class));
            }
        }
    }
    
    private static void checkGenericTransitionHasTheRightParameters(
            Method method) throws StateMachineException
    {
        Class<?> paramTypes[] = method.getParameterTypes();
        if (paramTypes == null || paramTypes.length != 1
                || !paramTypes[0].equals(TransitionInfo.class))
            throw new StateMachineException(
                    "Transition for method "
                            + method.getName()
                            + " is not well defined. It should have one and only TransitionEvent paramter");
    }
    
    private void checkEnterStateAnnotation(Object instance,
            StateMachineDefinitionImpl<S> definition, Method method, OnEnter ann)
            throws StateMachineException
    {
        // First of all, we check the parameters
        checkGenericTransitionHasTheRightParameters(method);
        
        // Second, we check the return type is correct
        Class<?> resultType = method.getReturnType();
        if (resultType == null
                || (!resultType.equals(EventInfo.class) && !("void".equals(resultType.getName()))))
        {
            throw new StateMachineException(
                    "Transition for method "
                            + method.getName()
                            + " is not well defined. Enter phase must return a EventInfo or void");
        }
        
        definition.defineEnterState(ann, method, enhanced == null ? instance
                : enhanced);
    }
    
    private void checkExitStateAnnotation(Object instance,
            StateMachineDefinitionImpl<S> definition, Method method, OnExit ann)
            throws StateMachineException
    {
        // First of all, we check the parameters
        checkGenericTransitionHasTheRightParameters(method);
        // Second, we check the return type is correct
        Class<?> resultType = method.getReturnType();
        if (resultType == null
                || (!resultType.equals(Boolean.class) && !("void".equals(resultType.getName()))))
        {
            throw new StateMachineException(
                    "Transition for method "
                            + method.getName()
                            + " is not well defined. Exit phase must return a boolean or void");
        }
        
        definition.defineExitState(ann, method, enhanced == null ? instance
                : enhanced);
    }
    
    private void checkStateAnnotation(Object instance,
            StateMachineDefinitionImpl<S> definition, Field field, State ann)
            throws StateMachineException
    {
        if (!isStringAndFinal(field))
            throw new StateMachineException("@State " + field.getName()
                    + " must be declared as public static final");
        
        try
        {
            String stateName = (String) field.get(instance);
            definition.defineState(definition.getActualState(stateName),
                    ann.isStart(),
                    ann.isFinal());
        }
        catch (IllegalAccessException e)
        {
            l.error("Error. This should never happen as we have checked the conditions before using reflection",
                    e);
        }
    }
    
    private void checkEventAnnotation(Object instance,
            StateMachineDefinitionImpl<S> definition, Field field, Event ann)
            throws StateMachineException
    {
        if (!isStringAndFinal(field))
            throw new StateMachineException("@Event " + field.getName()
                    + " must be declared as public static final");
        
        try
        {
            String eventName = (String) field.get(instance);
            definition.defineEvent(eventName);
        }
        catch (IllegalAccessException e)
        {
            l.error("ERROR. This should never happen as we have checked the conditions before using reflection",
                    e);
        }
        //        catch (EventAlreadyExistsException e)
        //        {
        //            throw new IllegalEventAnnotationException("@Event "
        //                    + field.getName() + " has been declared twice");
        //        }
    }
    
    /*
     * TODO. Define a set of tests for this functionality
     */
    private void checkTransitionAnnotation(Object instance,
            StateMachineDefinitionImpl<S> stateMachineDefinition,
            Method method, Transition ann) throws StateMachineException
    {
        // First of all, we check the parameters
        stateMachineDefinition.defineEvent(ann.event());
        if (method != null)
            checkGenericTransitionHasTheRightParameters(method);
        stateMachineDefinition.defineTransition(ann,
                method,
                enhanced == null ? instance : enhanced);
    }
    
    /**
     * We check that the annotated field is a public final String type
     * 
     * @return true if it conforms the condition, false otherwise.
     */
    private boolean isStringAndFinal(Field field)
    {
        return (field.getType().equals(String.class)
                && Modifier.isFinal(field.getModifiers()) && Modifier.isPublic(field.getModifiers()));
    }
    
}
