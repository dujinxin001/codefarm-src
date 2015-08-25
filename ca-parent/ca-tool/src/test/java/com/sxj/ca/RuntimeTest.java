package com.sxj.ca;

import java.io.IOException;

import org.junit.After;
import org.junit.Test;

public class RuntimeTest
{
    
    @After
    public void tearDown() throws Exception
    {
    }
    
    @Test
    public void test() throws IOException
    {
        // Runtime.getRuntime().exec("F:\\PSTools\\PsExec.exe -d -s F:\\GG.exe");
        System.out.println(Float.MAX_VALUE);
    }
    
}
