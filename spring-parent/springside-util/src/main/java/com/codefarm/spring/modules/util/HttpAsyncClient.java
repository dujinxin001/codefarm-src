package com.codefarm.spring.modules.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpAsyncClient
{
    
    // private static ClientAsyncConnectionManager niocm = null;
    
    private static PoolingNHttpClientConnectionManager niocm = null;
    
    private static CloseableHttpAsyncClient closeableHttpAsyncClient = null;
    
    private static final Logger logger = LoggerFactory
            .getLogger(HttpAsyncClient.class);
    
    /** 默认的HTTP响应实体编码 = "UTF-8" */
    private static final String DEFAULT_CHARSET = "UTF-8";
    
    static
    {
        try
        {
            ConnectingIOReactor reactor = new DefaultConnectingIOReactor();
            niocm = new PoolingNHttpClientConnectionManager(reactor);
            niocm.setMaxTotal(200);
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), HttpAsyncClient.class);
        }
    }
    
    private static CloseableHttpAsyncClient bulidAsyncClient()
    {
        if (closeableHttpAsyncClient != null)
        {
            HttpAsyncClientBuilder clientBuilder = HttpAsyncClients.custom()
                    .setConnectionManager(niocm);
            closeableHttpAsyncClient = clientBuilder.build();
        }
        
        return closeableHttpAsyncClient;
    }
    // <<Get>>
    
    /** Get
     * 
     * @param url
     *        请求url
     * @param callback
     *        回调方法 */
    public static void get(String url, FutureCallback<HttpResponse> callback)
    {
        try
        {
            logger.debug("Get [" + url + "] ...", HttpAsyncClient.class);
            CloseableHttpAsyncClient client = bulidAsyncClient();
            client.start();
            HttpGet getMethod = new HttpGet(url);
            client.execute(getMethod, callback);
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), HttpAsyncClient.class);
        }
        
    }
    
    // /////////////////////////////////////////////////////////////////////////
    // <<Post>>
    
    /** Post XML（使用默认字符集）
     * 
     * @param url
     *        请求的URL
     * @param xml
     *        XML格式请求内容
     * @param callback
     *        回调方法 */
    public static void postXml(String url, String xml,
            FutureCallback<HttpResponse> callback)
    {
        postXml(url, xml, DEFAULT_CHARSET, callback);
    }
    
    /** Post XML
     * 
     * @param url
     *        请求的URL
     * @param xml
     *        XML格式请求内容
     * @param requestCharset
     *        请求内容字符集
     * @param callback
     *        回调方法 */
    public static void postXml(String url, String xml, String requestCharset,
            FutureCallback<HttpResponse> callback)
    {
        post(url,
                xml,
                "text/xml; charset=" + requestCharset,
                "text/xml",
                requestCharset,
                callback);
    }
    
    /** Post JSON（使用默认字符集）
     * 
     * @param url
     *        请求的URL
     * @param json
     *        JSON格式请求内容
     * @param callback
     *        回调方法 */
    public static void postJson(String url, String json,
            FutureCallback<HttpResponse> callback)
    {
        postJson(url, json, DEFAULT_CHARSET, callback);
    }
    
    /** Post JSON
     * 
     * @param url
     *        请求的URL
     * @param json
     *        JSON格式请求内容
     * @param requestCharset
     *        请求内容字符集
     * @param callback
     *        回调方法 */
    public static void postJson(String url, String json, String requestCharset,
            FutureCallback<HttpResponse> callback)
    {
        post(url,
                json,
                "application/json; charset=" + requestCharset,
                "application/json",
                requestCharset,
                callback);
    }
    
    /** Post
     * 
     * @param url
     *        请求的URL
     * @param content
     *        请求内容
     * @param contentType
     *        请求内容类型，HTTP Header中的<code>Content-type</code>
     * @param mimeType
     *        请求内容MIME类型
     * @param requestCharset
     *        请求内容字符集
     * @param callback
     *        回调方法 */
    public static void post(String url, String content, String contentType,
            String mimeType, String requestCharset,
            FutureCallback<HttpResponse> callback)
    {
        try
        {
            CloseableHttpAsyncClient client = bulidAsyncClient();
            client.start();
            ContentType type = ContentType.create(mimeType, requestCharset);
            HttpPost post = new HttpPost(url);
            post.setHeader("Content-Type", contentType);
            HttpEntity requestEntity = new StringEntity(content, type);
            post.setEntity(requestEntity);
            
            client.execute(post, callback);
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), HttpAsyncClient.class);
        }
    }
    
    /** Post（使用 {@link UrlEncodedFormEntity} 封装参数）
     * 
     * @param url
     *        请求url
     * @param params
     *        请求参数
     * @param callback
     *        回调方法 */
    public static void post(String url, Map<String, String> params,
            FutureCallback<HttpResponse> callback)
    {
        post(url, params, DEFAULT_CHARSET, callback);
    }
    
    /** Post（使用 {@link UrlEncodedFormEntity} 封装参数）
     * 
     * @param url
     *        请求URL
     * @param params
     *        请求参数
     * @param paramEncoding
     *        请求参数编码
     * @param callback
     *        回调方法 */
    public static void post(String url, Map<String, String> params,
            String paramEncoding, FutureCallback<HttpResponse> callback)
    {
        logger.debug("Post [" + url + "] ...", HttpAsyncClient.class);
        try
        {
            CloseableHttpAsyncClient client = bulidAsyncClient();
            client.start();
            HttpPost post = new HttpPost(url);
            if (params != null)
            {
                List<NameValuePair> paramList = new ArrayList<NameValuePair>();
                for (String key : params.keySet())
                {
                    paramList.add(new BasicNameValuePair(key, params.get(key)));
                }
                UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(
                        paramList, paramEncoding);
                post.setEntity(formEntity);
            }
            
            client.execute(post, callback);
        }
        catch (Exception e)
        {
            logger.debug(e.getMessage(), HttpAsyncClient.class);
        }
    }
    
    // /////////////////////////////////////////////////////////////////////////
    // <<SSL Get>>
    
//    public static void sslGet(String url, Map<String, String> params,
//            String keyType, String keyPath, String keyPassword, int sslport,
//            String authString, FutureCallback<HttpResponse> callback)
//    {
//        try
//        {
//            AsyncScheme sch = getSslScheme(keyType,
//                    keyPath,
//                    keyPassword,
//                    sslport);
//            sslGet(url, params, sch, authString, callback);
//        }
//        catch (Exception e)
//        {
//            logger.debug(e.getMessage(), HttpAsyncClientUtil.class);
//        }
//    }
//    
//    private static void sslGet(String url, Map<String, String> params,
//            AsyncScheme sch, String authString,
//            FutureCallback<HttpResponse> callback)
//    {
//        logger.debug("SSL Get [" + url + "] ...",
//                HttpAsyncClientUtil.class);
//        
//        try
//        {
//            HttpAsyncClient client = new DefaultHttpAsyncClient(niocm);
//            IOReactorStatus state = client.getStatus();
//            if (!state.equals(IOReactorStatus.ACTIVE))
//            {
//                client.start();
//            }
//            
//            client.getConnectionManager().getSchemeRegistry().register(sch);
//            HttpGet getMethod = new HttpGet(url);
//            
//            if (authString != null)
//                getMethod.setHeader("Authorization", authString);
//            
//            if (params != null)
//            {
//                HttpParams httpParams = new BasicHttpParams();
//                for (String key : params.keySet())
//                {
//                    httpParams.setParameter(key, params.get(key));
//                }
//                getMethod.setParams(httpParams);
//            }
//            
//            client.execute(getMethod, callback);
//        }
//        catch (Exception e)
//        {
//            logger.debug(e.getMessage(), HttpAsyncClientUtil.class);
//        }
//    }
//    
    // /////////////////////////////////////////////////////////////////////////
    //    // <<SSL Post>>
    //    
    //    /** SSL Post XML
    //     * 
    //     * @param url
    //     *        请求的URL
    //     * @param xml
    //     *        XML请求内容
    //     * @param keyType
    //     *        密钥类型
    //     * @param keyPath
    //     *        密钥文件路径
    //     * @param keyPassword
    //     *        密钥文件密码
    //     * @param sslPort
    //     *        SSL端口
    //     * @param authString
    //     *        头部认证信息
    //     * @param callback
    //     *        回调方法 */
    //    public static void sslPostXml(String url, String xml, String keyType,
    //            String keyPath, String keyPassword, int sslPort, String authString,
    //            FutureCallback<HttpResponse> callback)
    //    {
    //        try
    //        {
    //            AsyncScheme sch = getSslScheme(keyType,
    //                    keyPath,
    //                    keyPassword,
    //                    sslPort);
    //            sslPost(url,
    //                    xml,
    //                    "text/xml; charset=" + DEFAULT_CHARSET,
    //                    "text/xml",
    //                    DEFAULT_CHARSET,
    //                    sch,
    //                    authString,
    //                    callback);
    //        }
    //        catch (Exception e)
    //        {
    //            logger.debug(e.getMessage(), HttpAsyncClientUtil.class);
    //        }
    //    }
    
    //    /** SSL Post JSON
    //     * 
    //     * @param url
    //     *        请求的URL
    //     * @param json
    //     *        JSON格式请求内容
    //     * @param keyType
    //     *        密钥类型
    //     * @param keyPath
    //     *        密钥文件路径
    //     * @param keyPassword
    //     *        密钥文件密码
    //     * @param sslPort
    //     *        SSL端口
    //     * @param authString
    //     *        头部认证信息
    //     * @param callback
    //     *        回调方法 */
    //    public static void sslPostJson(String url, String json, String keyType,
    //            String keyPath, String keyPassword, int sslPort, String authString,
    //            FutureCallback<HttpResponse> callback)
    //    {
    //        try
    //        {
    //            AsyncScheme sch = getSslScheme(keyType,
    //                    keyPath,
    //                    keyPassword,
    //                    sslPort);
    //            sslPost(url,
    //                    json,
    //                    "application/json; charset=" + DEFAULT_CHARSET,
    //                    "application/json",
    //                    DEFAULT_CHARSET,
    //                    sch,
    //                    authString,
    //                    callback);
    //        }
    //        catch (Exception e)
    //        {
    //            logger.debug(e.getMessage(), HttpAsyncClientUtil.class);
    //        }
    //    }
    
    //    /** SSL Post
    //     * 
    //     * @param url
    //     *        请求的URL
    //     * @param content
    //     *        请求内容
    //     * @param contentType
    //     *        请求内容类型，HTTP Header中的<code>Content-type</code>
    //     * @param mimeType
    //     *        请求内容MIME类型
    //     * @param requestCharset
    //     *        请求内容字符集
    //     * @param sch
    //     *        Scheme
    //     * @param authString
    //     *        头部信息中的<code>Authorization</code>
    //     * @param callback
    //     *        回调方法 */
    //    private static void sslPost(String url, String content, String contentType,
    //            String mimeType, String requestCharset, AsyncScheme sch,
    //            String authString, FutureCallback<HttpResponse> callback)
    //    {
    //        JscnLogger.debug("SSL Post [" + url + "] ...",
    //                HttpAsyncClientUtil.class);
    //        
    //        try
    //        {
    //            HttpAsyncClient client = new DefaultHttpAsyncClient(niocm);
    //            client.getConnectionManager().getSchemeRegistry().register(sch);
    //            IOReactorStatus state = client.getStatus();
    //            if (!state.equals(IOReactorStatus.ACTIVE))
    //            {
    //                client.start();
    //            }
    //            
    //            HttpPost post = new HttpPost(url);
    //            
    //            if (authString != null)
    //                post.setHeader("Authorization", authString);
    //            if (contentType != null)
    //                post.setHeader("Content-Type", contentType);
    //            ContentType type = ContentType.create(mimeType, requestCharset);
    //            HttpEntity requestEntity = new StringEntity(content, type);
    //            post.setEntity(requestEntity);
    //            
    //            client.execute(post, callback);
    //        }
    //        catch (Exception e)
    //        {
    //            logger.debug(e.getMessage(), HttpAsyncClientUtil.class);
    //        }
    //    }
    
    //    // /////////////////////////////////////////////////////////////////////////
    //    // <<内部辅助方法>>
    //    
    //    private static AsyncScheme getSslScheme(String keyType, String keyPath,
    //            String keyPassword, int sslPort) throws Exception
    //    {
    //        KeyStore trustStore = KeyStore.getInstance(keyType);
    //        FileInputStream instream = new FileInputStream(new File(keyPath));
    //        try
    //        {
    //            trustStore.load(instream, keyPassword.toCharArray());
    //        }
    //        finally
    //        {
    //            try
    //            {
    //                instream.close();
    //            }
    //            catch (Exception ignore)
    //            {
    //                ignore.printStackTrace();
    //            }
    //        }
    //        // 验证密钥源
    //        KeyManagerFactory kmf = KeyManagerFactory.getInstance("sunx509");
    //        kmf.init(trustStore, keyPassword.toCharArray());
    //        
    //        // 同位体验证信任决策源
    //        TrustManager[] trustManagers = { new MyX509TrustManager() };
    //        
    //        // 初始化安全套接字
    //        SSLContext sslContext = SSLContext.getInstance("SSL");
    //        sslContext.init(kmf.getKeyManagers(), trustManagers, null);
    //        SSLLayeringStrategy layeringStrategy = new SSLLayeringStrategy(
    //                sslContext);
    //        return new AsyncScheme("https", sslPort, layeringStrategy);
    //    }
}
