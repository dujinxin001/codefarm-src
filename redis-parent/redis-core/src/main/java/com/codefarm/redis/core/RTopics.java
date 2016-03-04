package com.codefarm.redis.core;


public interface RTopics
{
    /** 
     * Returns topic instance by name.
     *
     * @param name of the distributed topic
     * @return
     */
    <M> RTopic<M> getTopic(String name);
}
