package com.codefarm.mybatis.orm;

import org.junit.Test;

import com.codefarm.spring.modules.util.CaseFormatUtils;

public class CaseFormatUtilsTest
{
    
    @Test
    public void test()
    {
        String name = "TestUserEntity";
        System.out.println(CaseFormatUtils.camelToUnderScore(name));
    }
    
}
