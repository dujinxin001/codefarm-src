package com.codefarm.jsonrpc.demo.service;

import com.codefarm.jsonrpc.annotation.JsonRpcService;

@JsonRpcService("/api/math.htm")
public interface IMathService
{
    public Integer add(Integer a, Integer b);
}
