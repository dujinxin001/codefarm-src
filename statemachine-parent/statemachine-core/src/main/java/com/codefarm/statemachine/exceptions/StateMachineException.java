package com.codefarm.statemachine.exceptions;

public class StateMachineException extends Exception
{
    private static final long serialVersionUID = 1L;
    
    public StateMachineException(String msg)
    {
        super(msg);
    }
    
    public StateMachineException(String message, Throwable cause)
    {
        super(message, cause);
    }
    
    public StateMachineException(Throwable cause)
    {
        super(cause);
    }
}
