package com.sxj.web.tag.freemarker;

import com.sxj.web.tag.ATag;
import com.sxj.web.tag.FormTag;
import com.sxj.web.tag.TokenNameTag;
import com.sxj.web.tag.TokenTag;
import com.sxj.web.tag.TokenValueTag;

import freemarker.template.SimpleHash;

public class CsrfTags extends SimpleHash
{
    
    public CsrfTags()
    {
        put("a", new ATag());
        put("form", new FormTag());
        put("tokenName", new TokenNameTag());
        put("token", new TokenTag());
        put("tokenValue", new TokenValueTag());
    }
    
}
