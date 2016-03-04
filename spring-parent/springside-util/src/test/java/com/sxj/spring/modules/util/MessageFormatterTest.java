package com.sxj.spring.modules.util;

import org.junit.Test;

import com.codefarm.spring.modules.util.MessageFormatter;

import junit.framework.Assert;

public class MessageFormatterTest
{
    
    @Test
    public void test()
    {
        
        String arrayFormat = MessageFormatter.arrayFormat("http://{}/{}",
                "a",
                "b");
        System.out.println(arrayFormat);
        Assert.assertEquals("http://a/b", arrayFormat);
    }
    
}
