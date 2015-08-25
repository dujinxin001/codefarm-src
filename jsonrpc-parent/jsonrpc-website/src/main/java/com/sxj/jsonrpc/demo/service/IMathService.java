package com.sxj.jsonrpc.demo.service;

import com.sxj.jsonrpc.annotation.JsonRpcService;

@JsonRpcService("/api/math.htm")
public interface IMathService
{
    public Integer add(Integer a, Integer b);
}
