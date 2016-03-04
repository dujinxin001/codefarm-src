package com.sxj.spring.modules.util;

import org.junit.Test;

import com.codefarm.spring.modules.util.Identities;

public class IdentitiesTest
{
    
    @Test
    public void testIdentities()
    {
        String random32 = Identities.randomBase62(32);
        System.out.println(random32.length());
        String random30 = Identities.randomBase62(30);
        System.out.println(random30.length());
    }
    
}
