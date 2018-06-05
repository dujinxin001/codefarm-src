package com.codefarm.statemachine;

import java.util.HashMap;

import com.google.common.collect.Maps;

/**
 * Contains the transition's information. Besides the basic information (source,
 * target and event), we provide the object passed when processing the event and
 * a transition context map which is really helpful when we need to store information
 * between phases of the same transition.
 */
public class TransitionInfo extends EventInfo
{
    private String source;
    
    private String target;
    
    // We offer a generic repository for all the distinct phases of a transition
    private HashMap<String, Object> transitionContext;
    
    public TransitionInfo(String source, String event, String target,
            Object object)
    {
        super(event, object);
        this.source = source;
        this.target = target;
        this.transitionContext = Maps.newHashMap();
    }
    
    public String getSource()
    {
        return source;
    }
    
    public String getTarget()
    {
        return target;
    }
    
    public HashMap<String, Object> getTransitionContext()
    {
        return this.transitionContext;
    }
    
    public String toString()
    {
        return "[" + source + " + " + event + " -> " + target + "]";
        
    }
}
