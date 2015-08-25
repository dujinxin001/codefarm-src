package com.sxj.statemachine.strategy;

public class NonReentrantStrategy<S extends Enum<?>> extends
        ReentrantStrategy<S>
{
    public NonReentrantStrategy()
    {
        super();
    }
}
