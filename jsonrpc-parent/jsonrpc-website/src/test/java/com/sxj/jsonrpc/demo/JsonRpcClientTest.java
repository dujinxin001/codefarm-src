package com.sxj.jsonrpc.demo;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Test;

import com.sxj.jsonrpc.client.JsonRpcHttpClient;
import com.sxj.jsonrpc.demo.service.IMathService;

public class JsonRpcClientTest
{
    
    @Test
    public void testAdd() throws MalformedURLException
    {
        JsonRpcHttpClient client = new JsonRpcHttpClient(new URL(
                "http://localhost:8080/jsonrpc-website/api/math.htm"));
        IMathService service = client.createProxy(IMathService.class);
        System.out.println(service.add(1, 2));
    }
    
}
