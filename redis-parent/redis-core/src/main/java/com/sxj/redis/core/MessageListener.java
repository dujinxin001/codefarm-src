/**
 * Copyright 2014 Nikita Koksharov, Nickolay Borbit
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sxj.redis.core;

import java.util.EventListener;

/**
 * Listener for Redis messages published via RTopic Redisson object
 *
 * @author Nikita Koksharov
 *
 * @param <M> message
 *
 * @see org.redisson.core.RTopic
 */
public interface MessageListener<M> extends EventListener
{
    
    /**
     * Invokes on every message in topic
     *
     * @param msg topic message
     */
    void onMessage(M message);
    
}
