package com.sxj.jsonrpc.demo.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.sxj.jsonrpc.demo.service.IMathService;

@Service
public class MathService implements IMathService
{
    
    @Override
    public Integer add(Integer a, Integer b)
    {
        Assert.notNull(a);
        Assert.notNull(b);
        return a + b;
    }
    
}
