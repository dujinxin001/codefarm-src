package com.codefarm.ca.key;

import java.util.HashMap;
import java.util.Map;

public enum SSH2_KEY_TYPE
{
    OPENSSH, IETFSECSH;
    
    private static Map<SSH2_KEY_TYPE, String[]> tokens = new HashMap<SSH2_KEY_TYPE, String[]>();
    static
    {
        tokens.put(IETFSECSH, new String[] { "---- BEGIN SSH2 PUBLIC KEY ----",
                "---- END SSH2 PUBLIC KEY ----" });
        tokens.put(OPENSSH, new String[] { "ssh-rsa" });
    }
    
    public String[] getTokens()
    {
        return tokens.get(this);
    }
}
