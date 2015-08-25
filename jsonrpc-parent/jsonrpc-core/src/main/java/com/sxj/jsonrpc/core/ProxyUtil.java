package com.sxj.jsonrpc.core;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
* Utilities for create client proxies.
*/
public abstract class ProxyUtil
{
    private static final Logger LOGGER = Logger.getLogger(ProxyUtil.class.getName());
    
    /**
    * Creates a composite service using all of the given
    * services.
    *
    * @param classLoader the {@link ClassLoader}
    * @param services the service objects
    * @param allowMultipleInheritance whether or not to allow multiple inheritance
    * @return the object
    */
    public static Object createCompositeServiceProxy(ClassLoader classLoader,
            Object[] services, boolean allowMultipleInheritance)
    {
        return createCompositeServiceProxy(classLoader,
                services,
                null,
                allowMultipleInheritance);
    }
    
    /**
    * Creates a composite service using all of the given
    * services and implementing the given interfaces.
    *
    * @param classLoader the {@link ClassLoader}
    * @param services the service objects
    * @param serviceInterfaces the service interfaces
    * @param allowMultipleInheritance whether or not to allow multiple inheritance
    * @return the object
    */
    public static Object createCompositeServiceProxy(ClassLoader classLoader,
            Object[] services, Class<?>[] serviceInterfaces,
            boolean allowMultipleInheritance)
    {
        // get interfaces
        Set<Class<?>> interfaces = new HashSet<Class<?>>();
        if (serviceInterfaces != null)
        {
            interfaces.addAll(Arrays.asList(serviceInterfaces));
        }
        else
        {
            for (Object o : services)
            {
                interfaces.addAll(Arrays.asList(o.getClass().getInterfaces()));
            }
        }
        // build the service map
        final Map<Class<?>, Object> serviceMap = new HashMap<Class<?>, Object>();
        for (Class<?> clazz : interfaces)
        {
            // we will allow for this, but the first
            // object that was registered wins
            if (serviceMap.containsKey(clazz) && allowMultipleInheritance)
            {
                continue;
            }
            else if (serviceMap.containsKey(clazz))
            {
                throw new IllegalArgumentException(
                        "Multiple inheritance not allowed " + clazz.getName());
            }
            // find a service for this interface
            for (Object o : services)
            {
                if (clazz.isInstance(o))
                {
                    if (LOGGER.isLoggable(Level.FINE))
                    {
                        LOGGER.fine("Using " + o.getClass().getName() + " for "
                                + clazz.getName());
                    }
                    serviceMap.put(clazz, o);
                    break;
                }
            }
            // make sure we have one
            if (!serviceMap.containsKey(clazz))
            {
                throw new IllegalArgumentException(
                        "None of the provided services implement "
                                + clazz.getName());
            }
        }
        // now create the proxy
        return Proxy.newProxyInstance(classLoader,
                interfaces.toArray(new Class<?>[0]),
                new InvocationHandler()
                {
                    public Object invoke(Object proxy, Method method,
                            Object[] args) throws Throwable
                    {
                        Class<?> clazz = method.getDeclaringClass();
                        if (clazz == Object.class)
                        {
                            return proxyObjectMethods(method, proxy, args);
                        }
                        return method.invoke(serviceMap.get(clazz), args);
                    }
                });
    }
    
    protected static Object proxyObjectMethods(Method method,
            Object proxyObject, Object[] args)
    {
        String name = method.getName();
        if (name.equals("toString"))
        {
            return proxyObject.getClass().getName() + "@"
                    + System.identityHashCode(proxyObject);
        }
        if (name.equals("hashCode"))
        {
            return System.identityHashCode(proxyObject);
        }
        if (name.equals("equals"))
        {
            return proxyObject == args[0];
        }
        throw new RuntimeException(method.getName()
                + " is not a member of java.lang.Object");
    }
}