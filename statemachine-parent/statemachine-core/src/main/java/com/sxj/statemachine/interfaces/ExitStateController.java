package com.sxj.statemachine.interfaces;

import com.sxj.statemachine.TransitionInfo;

/**
 * The only transition phase that we could cancel the transition if we return
 * false
 */
public interface ExitStateController
{
    /**
     * If we return <code>false</code> we will cancel the transition
     * 
     * @return returns a boolean for continuing the transition or not.
     */
    Boolean execute(TransitionInfo event);
}
