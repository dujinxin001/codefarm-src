package com.sxj.web;

import com.sxj.web.csrf.CsrfGuard;
import com.sxj.web.xss.XssGuard;

public final class SingletonHolder
{
    public static final CsrfGuard CSRF = new CsrfGuard();
    
    public static final XssGuard XSS = new XssGuard();
}
