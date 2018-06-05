package com.codefarm.statemachine.interfaces;

import com.codefarm.statemachine.TransitionInfo;

/**
 * It corresponds to the transition phase itself. It's where most of the work shoul be done,
 * unless you have very specific needs.
 */
public interface TransitionController
{
    void execute(TransitionInfo event);
}
