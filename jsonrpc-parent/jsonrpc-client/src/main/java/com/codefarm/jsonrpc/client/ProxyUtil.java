package com.codefarm.jsonrpc.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import com.codefarm.jsonrpc.annotation.JsonRpcMethod;
import com.codefarm.jsonrpc.core.ReflectionUtil;

public class ProxyUtil extends com.codefarm.jsonrpc.core.ProxyUtil
{
    public static <T> T createClientProxy(ClassLoader classLoader,
            Class<T> proxyInterface, final JsonRpcClient client, Socket socket)
            throws IOException
    {
        // create and return the proxy
        return createClientProxy(classLoader,
                proxyInterface,
                false,
                client,
                socket.getInputStream(),
                socket.getOutputStream());
    }
    
    @SuppressWarnings("unchecked")
    public static <T> T createClientProxy(ClassLoader classLoader,
            Class<T> proxyInterface, final boolean useNamedParams,
            final JsonRpcClient client, final InputStream ips,
            final OutputStream ops)
    {
        // create and return the proxy
        return (T) Proxy.newProxyInstance(classLoader,
                new Class<?>[] { proxyInterface },
                new InvocationHandler()
                {
                    public Object invoke(Object proxy, Method method,
                            Object[] args) throws Throwable
                    {
                        if (method.getDeclaringClass() == Object.class)
                        {
                            return proxyObjectMethods(method, proxy, args);
                        }
                        Object arguments = ReflectionUtil.parseArguments(method,
                                args,
                                useNamedParams);
                        return client.invokeAndReadResponse(method.getName(),
                                arguments,
                                method.getGenericReturnType(),
                                ops,
                                ips);
                    }
                });
    }
    
    @SuppressWarnings("unchecked")
    public static <T> T createClientProxy(ClassLoader classLoader,
            Class<T> proxyInterface, final boolean useNamedParams,
            final JsonRpcHttpClient client,
            final Map<String, String> extraHeaders)
    {
        // create and return the proxy
        return (T) Proxy.newProxyInstance(classLoader,
                new Class<?>[] { proxyInterface },
                new InvocationHandler()
                {
                    public Object invoke(Object proxy, Method method,
                            Object[] args) throws Throwable
                    {
                        if (method.getDeclaringClass() == Object.class)
                        {
                            return proxyObjectMethods(method, proxy, args);
                        }
                        Object arguments = ReflectionUtil.parseArguments(method,
                                args,
                                useNamedParams);
                        String methodName = method.getName();
                        JsonRpcMethod methodAnnotation = method.getAnnotation(JsonRpcMethod.class);
                        if (methodAnnotation != null
                                && methodAnnotation.value() != null)
                        {
                            methodName = methodAnnotation.value();
                        }
                        return client.invoke(methodName,
                                arguments,
                                method.getGenericReturnType(),
                                extraHeaders);
                    }
                });
    }
    
    /**
    * Creates a {@link Proxy} of the given {@link proxyInterface}
    * that uses the given {@link JsonRpcHttpClient}.
    * @param <T> the proxy type
    * @param classLoader the {@link ClassLoader}
    * @param proxyInterface the interface to proxy
    * @param client the {@link JsonRpcHttpClient}
    * @return the proxied interface
    */
    public static <T> T createClientProxy(ClassLoader classLoader,
            Class<T> proxyInterface, final JsonRpcHttpClient client)
    {
        return createClientProxy(classLoader,
                proxyInterface,
                false,
                client,
                new HashMap<String, String>());
    }
}
