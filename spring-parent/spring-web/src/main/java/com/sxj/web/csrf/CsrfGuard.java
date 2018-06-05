/**
 * The OWASP CSRFGuard Project, BSD License
 * Eric Sheridan (eric@infraredsecurity.com), Copyright (c) 2011 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *    2. Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *    3. Neither the name of OWASP nor the names of its contributors may be used
 *       to endorse or promote products derived from this software without specific
 *       prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.sxj.web.csrf;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sxj.spring.modules.util.Identities;
import com.sxj.web.SingletonHolder;
import com.sxj.web.WebGuard;
import com.sxj.web.WebGuardException;
import com.sxj.web.actions.IAction;
import com.sxj.web.util.Streams;
import com.sxj.web.util.Writers;

public final class CsrfGuard implements WebGuard
{
    public final static String ACTION_PREFIX = "com.sxj.csrf.action.";
    
    public final static String PAGE_TOKENS_KEY = "com.sxj.csrf.page.tokens.key";
    
    private final static String PROTECTED_PAGE_PREFIX = "com.sxj.csrf.protected.";
    
    private final static String UNPROTECTED_PAGE_PREFIX = "com.sxj.csrf.unprotected.";
    
    private Logger logger = LoggerFactory.getLogger(CsrfGuard.class);
    
    private String tokenName = null;
    
    private int tokenLength = 32;
    
    private boolean rotate = false;
    
    private boolean tokenPerPage = false;
    
    private boolean tokenPerPagePrecreate;
    
    private SecureRandom prng = null;
    
    private String newTokenLandingPage = null;
    
    private boolean useNewTokenLandingPage = false;
    
    private boolean ajax = false;
    
    private boolean protect = false;
    
    private String sessionKey = null;
    
    private Set<String> protectedPages = null;
    
    private Set<String> unprotectedPages = null;
    
    private Set<String> protectedMethods = null;
    
    private List<IAction> actions = null;
    
    public static CsrfGuard getInstance()
    {
        return SingletonHolder.CSRF;
    }
    
    public void load(Properties properties)
    {
        try
        {
            CsrfGuard csrfGuard = SingletonHolder.CSRF;
            
            /** load simple properties **/
            csrfGuard.setTokenName(properties.getProperty("com.sxj.csrf.TokenName",
                    "COM_SXJ_GUARD_TOKENNAME"));
            csrfGuard.setTokenLength(Integer.parseInt(properties.getProperty("com.sxj.csrf.TokenLength",
                    "32")));
            csrfGuard.setRotate(Boolean.valueOf(properties.getProperty("com.sxj.csrf.Rotate",
                    "false")));
            csrfGuard.setTokenPerPage(Boolean.valueOf(properties.getProperty("com.sxj.csrf.TokenPerPage",
                    "false")));
            csrfGuard.setTokenPerPagePrecreate(Boolean.valueOf(properties.getProperty("com.sxj.csrf.TokenPerPagePrecreate",
                    "false")));
            csrfGuard.setPrng(SecureRandom.getInstance(properties.getProperty("com.sxj.csrf.PRNG",
                    "SHA1PRNG"),
                    properties.getProperty("com.sxj.csrf.PRNG.Provider", "SUN")));
            csrfGuard.setNewTokenLandingPage(properties.getProperty("com.sxj.csrf.NewTokenLandingPage"));
            
            //default to false if newTokenLandingPage is not set; default to true if set.
            if (csrfGuard.getNewTokenLandingPage() == null)
            {
                csrfGuard.setUseNewTokenLandingPage(Boolean.valueOf(properties.getProperty("com.sxj.csrf.UseNewTokenLandingPage",
                        "false")));
            }
            else
            {
                csrfGuard.setUseNewTokenLandingPage(Boolean.valueOf(properties.getProperty("com.sxj.csrf.UseNewTokenLandingPage",
                        "true")));
            }
            csrfGuard.setSessionKey(properties.getProperty("com.sxj.csrf.SessionKey",
                    "COM_SXJ_GUARD_KEY"));
            csrfGuard.setAjax(Boolean.valueOf(properties.getProperty("com.sxj.csrf.Ajax",
                    "false")));
            csrfGuard.setProtect(Boolean.valueOf(properties.getProperty("com.sxj.csrf.Protect",
                    "false")));
            
            initActions(properties);
            initPages(properties);
            initMethods(properties);
        }
        catch (Exception e)
        {
            getLogger().debug("Eror initailize CsrfGuard", e);
        }
    }
    
    private void initMethods(Properties properties)
    {
        /** initialize protected methods **/
        String methodList = properties.getProperty("com.sxj.csrf.ProtectedMethods");
        if (methodList != null && methodList.trim().length() != 0)
        {
            for (String method : methodList.split(","))
            {
                getInstance().getProtectedMethods().add(method.trim());
            }
        }
    }
    
    private void initPages(Properties properties)
    {
        /** initialize protected, unprotected pages **/
        for (Object obj : properties.keySet())
        {
            String key = (String) obj;
            
            if (key.startsWith(PROTECTED_PAGE_PREFIX))
            {
                String directive = key.substring(PROTECTED_PAGE_PREFIX.length());
                int index = directive.indexOf('.');
                
                /** page name/class **/
                if (index < 0)
                {
                    String pageUri = properties.getProperty(key);
                    
                    getInstance().getProtectedPages().add(pageUri);
                }
            }
            
            if (key.startsWith(UNPROTECTED_PAGE_PREFIX))
            {
                String directive = key.substring(UNPROTECTED_PAGE_PREFIX.length());
                int index = directive.indexOf('.');
                
                /** page name/class **/
                if (index < 0)
                {
                    String pageUri = properties.getProperty(key);
                    
                    getInstance().getUnprotectedPages().add(pageUri);
                }
            }
        }
    }
    
    private void initActions(Properties properties)
            throws InstantiationException, IllegalAccessException,
            ClassNotFoundException, IOException
    {
        /** first pass: instantiate actions **/
        Map<String, IAction> actionsMap = new HashMap<String, IAction>();
        
        for (Object obj : properties.keySet())
        {
            String key = (String) obj;
            
            if (key.startsWith(ACTION_PREFIX))
            {
                String directive = key.substring(ACTION_PREFIX.length());
                int index = directive.indexOf('.');
                
                /** action name/class **/
                if (index < 0)
                {
                    String actionClass = properties.getProperty(key);
                    IAction action = (IAction) Class.forName(actionClass)
                            .newInstance();
                    
                    action.setName(directive);
                    actionsMap.put(action.getName(), action);
                    getInstance().getActions().add(action);
                }
            }
        }
        
        /** second pass: initialize action parameters **/
        for (Object obj : properties.keySet())
        {
            String key = (String) obj;
            
            if (key.startsWith(ACTION_PREFIX))
            {
                String directive = key.substring(ACTION_PREFIX.length());
                int index = directive.indexOf('.');
                
                /** action name/class **/
                if (index >= 0)
                {
                    String actionName = directive.substring(0, index);
                    IAction action = actionsMap.get(actionName);
                    
                    if (action == null)
                    {
                        throw new IOException(
                                String.format("action class %s has not yet been specified",
                                        actionName));
                    }
                    
                    String parameterName = directive.substring(index + 1);
                    String parameterValue = properties.getProperty(key);
                    
                    action.setParameter(parameterName, parameterValue);
                }
            }
        }
        
        /** ensure at least one action was defined **/
        if (getInstance().getActions().size() <= 0)
        {
            throw new IOException("failure to define at least one action");
        }
    }
    
    public CsrfGuard()
    {
        actions = new ArrayList<IAction>();
        protectedPages = new HashSet<String>();
        unprotectedPages = new HashSet<String>();
        protectedMethods = new HashSet<String>();
    }
    
    public String getTokenName()
    {
        return tokenName;
    }
    
    private void setTokenName(String tokenName)
    {
        this.tokenName = tokenName;
    }
    
    public int getTokenLength()
    {
        return tokenLength;
    }
    
    private void setTokenLength(int tokenLength)
    {
        this.tokenLength = tokenLength;
    }
    
    public boolean isRotateEnabled()
    {
        return rotate;
    }
    
    private void setRotate(boolean rotate)
    {
        this.rotate = rotate;
    }
    
    public boolean isTokenPerPageEnabled()
    {
        return tokenPerPage;
    }
    
    private void setTokenPerPage(boolean tokenPerPage)
    {
        this.tokenPerPage = tokenPerPage;
    }
    
    public boolean isTokenPerPagePrecreate()
    {
        return tokenPerPagePrecreate;
    }
    
    private void setTokenPerPagePrecreate(boolean tokenPerPagePrecreate)
    {
        this.tokenPerPagePrecreate = tokenPerPagePrecreate;
    }
    
    public SecureRandom getPrng()
    {
        return prng;
    }
    
    private void setPrng(SecureRandom prng)
    {
        this.prng = prng;
    }
    
    public String getNewTokenLandingPage()
    {
        return newTokenLandingPage;
    }
    
    private void setNewTokenLandingPage(String newTokenLandingPage)
    {
        this.newTokenLandingPage = newTokenLandingPage;
    }
    
    public boolean isUseNewTokenLandingPage()
    {
        return useNewTokenLandingPage;
    }
    
    private void setUseNewTokenLandingPage(boolean useNewTokenLandingPage)
    {
        this.useNewTokenLandingPage = useNewTokenLandingPage;
    }
    
    public boolean isAjaxEnabled()
    {
        return ajax;
    }
    
    private void setAjax(boolean ajax)
    {
        this.ajax = ajax;
    }
    
    public boolean isProtectEnabled()
    {
        return protect;
    }
    
    public void setProtect(boolean protect)
    {
        this.protect = protect;
    }
    
    public String getSessionKey()
    {
        return sessionKey;
    }
    
    private void setSessionKey(String sessionKey)
    {
        this.sessionKey = sessionKey;
    }
    
    public Set<String> getProtectedPages()
    {
        return protectedPages;
    }
    
    public Set<String> getUnprotectedPages()
    {
        return unprotectedPages;
    }
    
    public Set<String> getProtectedMethods()
    {
        return protectedMethods;
    }
    
    public List<IAction> getActions()
    {
        return actions;
    }
    
    public String getTokenValue(HttpServletRequest request)
    {
        return getTokenValue(request, request.getRequestURI());
    }
    
    public String getTokenValue(HttpSession session)
    {
        String tokenValue = (String) session.getAttribute(getSessionKey());
        
        if (tokenValue == null)
        {
            tokenValue = Identities.randomBase62(getTokenLength());
            session.setAttribute(getSessionKey(), tokenValue);
        }
        return tokenValue;
    }
    
    public String getTokenValue(HttpServletRequest request, String uri)
    {
        String tokenValue = null;
        HttpSession session = request.getSession();
        
        if (session != null && isProtectedPage(uri))
        {
            if (isTokenPerPageEnabled())
            {
                Map<String, String> pageTokens = getPageTokens(session);
                
                if (pageTokens != null)
                {
                    if (isTokenPerPagePrecreate())
                    {
                        createPageToken(pageTokens, uri, session);
                    }
                    tokenValue = pageTokens.get(uri);
                    
                }
            }
            
            if (tokenValue == null)
            {
                tokenValue = (String) session.getAttribute(getSessionKey());
            }
        }
        
        return tokenValue;
    }
    
    private Map<String, String> getPageTokens(HttpSession session)
    {
        
        Map<String, String> pageTokens = (Map<String, String>) session.getAttribute(CsrfGuard.PAGE_TOKENS_KEY);
        if (pageTokens == null)
        {
            pageTokens = new HashMap<String, String>();
            session.setAttribute(CsrfGuard.PAGE_TOKENS_KEY, pageTokens);
        }
        return pageTokens;
    }
    
    @Override
    public boolean isValidRequest(HttpServletRequest request,
            HttpServletResponse response)
    {
        boolean valid = isProtectedPageAndMethod(request);
        HttpSession session = request.getSession();
        String tokenFromSession = (String) session.getAttribute(getSessionKey());
        
        /** sending request to protected resource - verify token **/
        if (valid)
        {
            if (tokenFromSession != null)
            {
                try
                {
                    if (isAjaxEnabled() && isAjaxRequest(request))
                    {
                        verifyAjaxToken(request);
                    }
                    else if (isTokenPerPageEnabled())
                    {
                        verifyPageToken(request);
                    }
                    else
                    {
                        verifySessionToken(request);
                    }
                    valid = true;
                }
                catch (WebGuardException csrfe)
                {
                    getLogger().error("", csrfe);
                    invokeActions(request, response, csrfe);
                }
                
                /** rotate session and page tokens **/
                if (!isAjaxRequest(request) && isRotateEnabled())
                {
                    rotateTokens(request);
                }
                /** expected token in session - bad state **/
            }
            else if (tokenFromSession == null)
            {
                throw new IllegalStateException(
                        "CsrfGuard expects the token to exist in session at this point");
            }
        }
        else
        {
            /** unprotected page - nothing to do **/
        }
        
        return true;
    }
    
    private void invokeActions(HttpServletRequest request,
            HttpServletResponse response, WebGuardException csrfe)
    {
        for (IAction action : getActions())
        {
            try
            {
                action.execute(request, response, csrfe);
            }
            catch (WebGuardException exception)
            {
                getLogger().error(exception.getMessage(), exception);
            }
        }
    }
    
    public void updateToken(HttpSession session)
    {
        String tokenValue = (String) session.getAttribute(getSessionKey());
        
        /** Generate a new token and store it in the session. **/
        if (tokenValue == null)
        {
            try
            {
                tokenValue = Identities.randomBase62(getTokenLength());
            }
            catch (Exception e)
            {
                throw new RuntimeException(
                        String.format("unable to generate the random token - %s",
                                e.getLocalizedMessage()), e);
            }
            
            session.setAttribute(getSessionKey(), tokenValue);
        }
    }
    
    public void updateTokens(HttpServletRequest request)
    {
        /** cannot create sessions if response already committed **/
        HttpSession session = request.getSession(false);
        
        if (session != null)
        {
            /** create master token if it does not exist **/
            updateToken(session);
            
            /** create page specific token **/
            if (isTokenPerPageEnabled())
            {
                Map<String, String> pageTokens = getPageTokens(session);
                
                /** create token if it does not exist **/
                if (isProtectedPageAndMethod(request))
                {
                    createPageToken(pageTokens,
                            request.getRequestURI(),
                            session);
                }
            }
        }
    }
    
    /**
     * Create page token if it doesn't exist.
     * @param pageTokens A map of tokens. If token doesn't exist it will be added.
     * @param uri The key for the tokens.
     */
    private void createPageToken(Map<String, String> pageTokens, String uri,
            HttpSession session)
    {
        
        if (pageTokens == null)
            return;
        
        /** create token if it does not exist **/
        if (pageTokens.containsKey(uri))
            return;
        try
        {
            pageTokens.put(uri, Identities.randomBase62(getTokenLength()));
            session.setAttribute(CsrfGuard.PAGE_TOKENS_KEY, pageTokens);
        }
        catch (Exception e)
        {
            throw new RuntimeException(
                    String.format("unable to generate the random token - %s",
                            e.getLocalizedMessage()), e);
        }
    }
    
    public void writeLandingPage(HttpServletRequest request,
            HttpServletResponse response) throws IOException
    {
        String landingPage = getNewTokenLandingPage();
        
        /** default to current page **/
        if (landingPage == null)
        {
            StringBuilder sb = new StringBuilder();
            
            sb.append(request.getContextPath());
            sb.append(request.getServletPath());
            
            landingPage = sb.toString();
        }
        
        /** create auto posting form **/
        StringBuilder sb = new StringBuilder();
        
        sb.append("<html>\r\n");
        sb.append("<head>\r\n");
        sb.append("<title>OWASP CSRFGuard Project - New Token Landing Page</title>\r\n");
        sb.append("</head>\r\n");
        sb.append("<body>\r\n");
        sb.append("<script type=\"text/javascript\">\r\n");
        sb.append("var form = document.createElement(\"form\");\r\n");
        sb.append("form.setAttribute(\"method\", \"post\");\r\n");
        sb.append("form.setAttribute(\"action\", \"");
        sb.append(landingPage);
        sb.append("\");\r\n");
        
        /** only include token if needed **/
        if (isProtectedPage(landingPage))
        {
            sb.append("var hiddenField = document.createElement(\"input\");\r\n");
            sb.append("hiddenField.setAttribute(\"type\", \"hidden\");\r\n");
            sb.append("hiddenField.setAttribute(\"name\", \"");
            sb.append(getTokenName());
            sb.append("\");\r\n");
            sb.append("hiddenField.setAttribute(\"value\", \"");
            sb.append(getTokenValue(request, landingPage));
            sb.append("\");\r\n");
            sb.append("form.appendChild(hiddenField);\r\n");
        }
        
        sb.append("document.body.appendChild(form);\r\n");
        sb.append("form.submit();\r\n");
        sb.append("</script>\r\n");
        sb.append("</body>\r\n");
        sb.append("</html>\r\n");
        
        String code = sb.toString();
        
        /** setup headers **/
        response.setContentType("text/html");
        response.setContentLength(code.length());
        
        /** write auto posting form **/
        OutputStream output = null;
        PrintWriter writer = null;
        
        try
        {
            output = response.getOutputStream();
            writer = new PrintWriter(output);
            
            writer.write(code);
            writer.flush();
        }
        finally
        {
            Writers.close(writer);
            Streams.close(output);
        }
    }
    
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        
        sb.append("\r\n*****************************************************\r\n");
        sb.append("* Owasp.CsrfGuard Properties\r\n");
        sb.append("*\r\n");
        sb.append(String.format("* Logger: %s\r\n", getLogger().getClass()
                .getName()));
        sb.append(String.format("* NewTokenLandingPage: %s\r\n",
                getNewTokenLandingPage()));
        sb.append(String.format("* PRNG: %s\r\n", getPrng().getAlgorithm()));
        sb.append(String.format("* SessionKey: %s\r\n", getSessionKey()));
        sb.append(String.format("* TokenLength: %s\r\n", getTokenLength()));
        sb.append(String.format("* TokenName: %s\r\n", getTokenName()));
        sb.append(String.format("* Ajax: %s\r\n", isAjaxEnabled()));
        sb.append(String.format("* Rotate: %s\r\n", isRotateEnabled()));
        sb.append(String.format("* TokenPerPage: %s\r\n",
                isTokenPerPageEnabled()));
        
        for (IAction action : actions)
        {
            sb.append(String.format("* Action: %s\r\n", action.getClass()
                    .getName()));
            
            for (String name : action.getParameterMap().keySet())
            {
                String value = action.getParameter(name);
                
                sb.append(String.format("*\tParameter: %s = %s\r\n",
                        name,
                        value));
            }
        }
        sb.append("*****************************************************\r\n");
        
        return sb.toString();
    }
    
    private boolean isAjaxRequest(HttpServletRequest request)
    {
        return request.getHeader("X-Requested-With") != null;
    }
    
    private void verifyAjaxToken(HttpServletRequest request)
            throws WebGuardException
    {
        HttpSession session = request.getSession(true);
        String tokenFromSession = (String) session.getAttribute(getSessionKey());
        String tokenFromRequest = request.getHeader(getTokenName());
        
        if (tokenFromRequest == null)
        {
            /** FAIL: token is missing from the request **/
            throw new WebGuardException(
                    "required token is missing from the request");
        }
        else if (!tokenFromSession.equals(tokenFromRequest))
        {
            /** FAIL: the request token does not match the session token **/
            throw new WebGuardException(
                    "request token does not match session token");
        }
    }
    
    private void verifyPageToken(HttpServletRequest request)
            throws WebGuardException
    {
        HttpSession session = request.getSession(false);
        Map<String, String> pageTokens = getPageTokens(session);
        
        String tokenFromPages = (pageTokens != null ? pageTokens.get(request.getRequestURI())
                : null);
        String tokenFromSession = (String) session.getAttribute(getSessionKey());
        String tokenFromRequest = request.getParameter(getTokenName());
        
        if (tokenFromRequest == null)
        {
            /** FAIL: token is missing from the request **/
            throw new WebGuardException(
                    "required token is missing from the request");
        }
        else if (tokenFromPages != null)
        {
            if (!tokenFromPages.equals(tokenFromRequest))
            {
                /** FAIL: request does not match page token **/
                throw new WebGuardException(
                        "request token does not match page token");
            }
        }
        else if (!tokenFromSession.equals(tokenFromRequest))
        {
            /** FAIL: the request token does not match the session token **/
            throw new WebGuardException(
                    "request token does not match session token");
        }
        /** update tokens **/
        updateTokens(request);
    }
    
    private void verifySessionToken(HttpServletRequest request)
            throws WebGuardException
    {
        HttpSession session = request.getSession(true);
        String tokenFromSession = (String) session.getAttribute(getSessionKey());
        String tokenFromRequest = request.getParameter(getTokenName());
        
        if (tokenFromRequest == null)
        {
            /** FAIL: token is missing from the request **/
            throw new WebGuardException(
                    "required token is missing from the request");
        }
        else if (!tokenFromSession.equals(tokenFromRequest))
        {
            /** FAIL: the request token does not match the session token **/
            throw new WebGuardException(
                    "request token does not match session token");
        }
        /** update tokens **/
        updateTokens(request);
    }
    
    private void rotateTokens(HttpServletRequest request)
    {
        HttpSession session = request.getSession(true);
        
        /** rotate master token **/
        String tokenFromSession = null;
        
        try
        {
            tokenFromSession = Identities.randomBase62(getTokenLength());
        }
        catch (Exception e)
        {
            throw new RuntimeException(
                    String.format("unable to generate the random token - %s",
                            e.getLocalizedMessage()), e);
        }
        
        session.setAttribute(getSessionKey(), tokenFromSession);
        
        /** rotate page token **/
        if (isTokenPerPageEnabled())
        {
            @SuppressWarnings("unchecked")
            Map<String, String> pageTokens = getPageTokens(session);
            
            try
            {
                pageTokens.put(request.getRequestURI(),
                        Identities.randomBase62(getTokenLength()));
                session.setAttribute(CsrfGuard.PAGE_TOKENS_KEY, pageTokens);
            }
            catch (Exception e)
            {
                throw new RuntimeException(
                        String.format("unable to generate the random token - %s",
                                e.getLocalizedMessage()), e);
            }
        }
    }
    
    public boolean isProtectedPage(String uri)
    {
        boolean retval = !isProtectEnabled();
        
        for (String protectedPage : protectedPages)
        {
            if (isUriExactMatch(protectedPage, uri))
            {
                return true;
            }
            else if (isUriMatch(protectedPage, uri))
            {
                retval = true;
            }
        }
        
        for (String unprotectedPage : unprotectedPages)
        {
            if (isUriExactMatch(unprotectedPage, uri))
            {
                return false;
            }
            else if (isUriMatch(unprotectedPage, uri))
            {
                retval = false;
            }
        }
        
        return retval;
    }
    
    public boolean isProtectedMethod(String method)
    {
        boolean retval = false;
        
        if (protectedMethods.isEmpty() || protectedMethods.contains(method))
        {
            retval = true;
        }
        
        return retval;
    }
    
    public boolean isProtectedPageAndMethod(String page, String method)
    {
        return isProtectedMethod(method) && isProtectedPage(page);
    }
    
    public boolean isProtectedPageAndMethod(HttpServletRequest request)
    {
        return isProtectedPageAndMethod(request.getRequestURI()
                .split(request.getContextPath())[1],
                request.getMethod());
    }
    
    /**
     * FIXME: taken from Tomcat - ApplicationFilterFactory
     * 
     * @param testPath the pattern to match.
     * @param requestPath the current request path.
     * @return {@code true} if {@code requestPath} matches {@code testPath}.
     */
    private boolean isUriMatch(String testPath, String requestPath)
    {
        boolean retval = false;
        
        /** Case 1: Exact Match **/
        if (testPath.equals(requestPath))
        {
            retval = true;
        }
        
        /** Case 2 - Path Match ("/.../*") **/
        if (testPath.equals("/*"))
        {
            retval = true;
        }
        if (testPath.endsWith("/*"))
        {
            if (testPath.regionMatches(0, requestPath, 0, testPath.length() - 2))
            {
                if (requestPath.length() == (testPath.length() - 2))
                {
                    retval = true;
                }
                else if ('/' == requestPath.charAt(testPath.length() - 2))
                {
                    retval = true;
                }
            }
        }
        
        /** Case 3 - Extension Match **/
        if (testPath.startsWith("*."))
        {
            int slash = requestPath.lastIndexOf('/');
            int period = requestPath.lastIndexOf('.');
            
            if ((slash >= 0)
                    && (period > slash)
                    && (period != requestPath.length() - 1)
                    && ((requestPath.length() - period) == (testPath.length() - 1)))
            {
                retval = testPath.regionMatches(2,
                        requestPath,
                        period + 1,
                        testPath.length() - 2);
            }
        }
        
        return retval;
    }
    
    private boolean isUriExactMatch(String testPath, String requestPath)
    {
        boolean retval = false;
        
        /** Case 1: Exact Match **/
        if (testPath.equals(requestPath))
        {
            retval = true;
        }
        
        return retval;
    }
    
    public Logger getLogger()
    {
        return logger;
    }
    
}
