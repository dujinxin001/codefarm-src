package com.sxj.web.tag;

import javax.servlet.jsp.JspException;

import com.sxj.web.csrf.CsrfGuard;

public class AjaxEnabledTag extends AbstractUriTag
{
    
    @Override
    public int doStartTag() throws JspException
    {
        if (CsrfGuard.getInstance().isAjaxEnabled())
            return EVAL_BODY_INCLUDE;
        return SKIP_BODY;
    }
    
}
