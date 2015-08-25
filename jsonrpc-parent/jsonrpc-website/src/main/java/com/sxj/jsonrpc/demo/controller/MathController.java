package com.sxj.jsonrpc.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sxj.jsonrpc.demo.dto.FormBean;
import com.sxj.jsonrpc.demo.service.IMathService;

@Controller
@RequestMapping("/math")
public class MathController
{
    @Autowired
    IMathService mathService;
    
    @RequestMapping("/add")
    @ResponseBody
    public Integer add(Integer a, Integer b)
    {
        return mathService.add(a, b);
    }
    
    @RequestMapping("/echo")
    @ResponseBody
    public String echo(String message)
    {
        return message;
    }
    
    @RequestMapping("/get")
    @ResponseBody
    public FormBean get(String message)
    {
        FormBean bean = new FormBean();
        bean.setMessage(message);
        return bean;
    }
}
