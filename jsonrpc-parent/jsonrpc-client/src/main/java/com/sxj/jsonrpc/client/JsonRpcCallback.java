package com.sxj.jsonrpc.client;

public interface JsonRpcCallback<T>
{
    
    /**
     * Called if the remote invocation was successful.
     *
     * @param result the result object of the call (possibly null)
     */
    void onComplete(T result);
    
    /**
     * Called if there was an error in the remove invocation.
     *
     * @param t the {@code Throwable} (possibly wrapping) the invocation error
     */
    void onError(Throwable t);
}
