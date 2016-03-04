package com.codefarm.spring.modules.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

public class HttpClient
{
    /** 默认的HTTP响应实体编码 = "UTF-8" */
    private String charset = "UTF-8";
    
    private String keyStoreType = "jks";
    
    private String keyStorePath;
    
    private String keyPassword;
    
    /**
     * HTTP Get
     * <p/>
     * 响应内容实体采用<code>UTF-8</code>字符集
     * 
     * @param url
     *            请求url
     * @return 响应内容实体
     * @throws IOException
     * @throws ClientProtocolException
     */
    public String get(String url) throws ClientProtocolException, IOException
    {
        return get(url, getCharset());
    }
    
    public String get(String url, Header... headers)
            throws ClientProtocolException, IOException
    {
        if (headers == null)
            return get(url, getCharset());
        else
            return get(url, getCharset(), headers);
        
    }
    
    /**
     * HTTP Post
     * 
     * @param url
     *            请求url
     * @param params
     *            请求参数
     * @return 响应内容实体
     * @throws IOException
     * @throws ClientProtocolException
     */
    public String post(String url, Map<String, String> params)
            throws ClientProtocolException, IOException
    {
        return post(url, params, getCharset(), getCharset());
    }
    
    /**
     * HTTP Post XML（使用默认字符集）
     * 
     * @param url
     *            请求的URL
     * @param xml
     *            XML格式请求内容
     * @return 响应内容实体
     * @throws IOException
     * @throws ClientProtocolException
     */
    public String postXml(String url, String xml)
            throws ClientProtocolException, IOException
    {
        return postXml(url, xml, getCharset(), getCharset());
    }
    
    /**
     * HTTP Post JSON（使用默认字符集）
     * 
     * @param url
     *            请求的URL
     * @param json
     *            JSON格式请求内容
     * @return 响应内容实体
     * @throws IOException
     * @throws ClientProtocolException
     */
    public String postJson(String url, String json)
            throws ClientProtocolException, IOException
    {
        return postJson(url, json, getCharset(), getCharset());
    }
    
    /**
     * SSLGET
     * 
     * @param url
     * @param params
     * @param keyType
     * @param keyPath
     * @param keyPassword
     * @param sslport
     * @param authString
     * @return
     * @throws Exception
     */
    
    public String sslGet(String url, String authString) throws Exception
    {
        CloseableHttpClient sslClient = getSslHttpClient(getKeyStoreType(),
                getKeyStorePath(),
                getKeyPassword());
        return sslGet(url, sslClient, authString);
        
    }
    
    /**
     * SSLHTTP Post
     * 
     * @param url
     *            请求url
     * @param params
     *            请求参数
     * @return 响应内容实体
     * @throws Exception
     */
    public String sslPost(String url, Map<String, String> params,
            String authString) throws Exception
    {
        CloseableHttpClient client = getSslHttpClient(getKeyStoreType(),
                getKeyStorePath(),
                getKeyPassword());
        HttpPost post = new HttpPost(url);
        if (params != null)
        {
            List<NameValuePair> paramList = new ArrayList<NameValuePair>();
            for (String key : params.keySet())
            {
                paramList.add(new BasicNameValuePair(key, params.get(key)));
            }
            UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(
                    paramList, getCharset());
            post.setEntity(formEntity);
        }
        HttpResponse response = client.execute(post);
        return consumeResponseEntity(response, getCharset());
    }
    
    /**
     * SSL Post XML（使用默认字符集）
     * 
     * @param url
     *            请求的URL
     * @param xml
     *            XML格式请求内容
     * @param keyType
     *            密钥类型
     * @param keyPath
     *            密钥文件路径
     * @param keyPassword
     *            密钥文件密码
     * @param sslPort
     *            SSL端口
     * @param authString
     *            头部认证信息
     * @return 响应内容实体
     * @throws Exception
     */
    public String sslPostXml(String url, String xml, String authString)
            throws Exception
    {
        CloseableHttpClient sch = getSslHttpClient(getKeyStoreType(),
                getKeyStorePath(),
                getKeyPassword());
        return sslPost(url,
                xml,
                "text/xml; charset=" + getCharset(),
                "text/xml",
                getCharset(),
                getCharset(),
                sch,
                authString);
        
    }
    
    /**
     * SSL Post JSON（使用默认字符集）
     * 
     * @param url
     *            请求的URL
     * @param json
     *            JSON格式请求内容
     * @param keyType
     *            密钥类型
     * @param keyPath
     *            密钥文件路径
     * @param keyPassword
     *            密钥文件密码
     * @param sslPort
     *            SSL端口
     * @param authString
     *            头部认证信息
     * @return 响应内容实体
     * @throws Exception
     */
    public String sslPostJson(String url, String json, String authString)
            throws Exception
    {
        CloseableHttpClient sch = getSslHttpClient(getKeyStoreType(),
                getKeyStorePath(),
                getKeyPassword());
        return sslPost(url,
                json,
                "application/json; charset=" + getCharset(),
                "application/json",
                getCharset(),
                getCharset(),
                sch,
                authString);
        
    }
    
    // /////////////////////////////////////////////////////////////////////////
    // <<内部辅助方法>>
    /**
     * HTTP Get
     * 
     * @param url
     *            请求url
     * @param responseCharset
     *            响应内容字符集
     * @return 响应内容实体
     * @throws IOException
     * @throws ClientProtocolException
     */
    private String get(String url, String responseCharset)
            throws ClientProtocolException, IOException
    {
        CloseableHttpClient client = HttpClientBuilder.create().build();
        HttpGet getMethod = new HttpGet(url);
        HttpResponse response = client.execute(getMethod);
        return consumeResponseEntity(response, responseCharset);
        
    }
    
    private String get(String url, String responseCharset, Header... headers)
            throws ClientProtocolException, IOException
    {
        CloseableHttpClient client = HttpClientBuilder.create().build();
        HttpGet getMethod = new HttpGet(url);
        for (Header header : headers)
            getMethod.addHeader(header);
        HttpResponse response = client.execute(getMethod);
        return consumeResponseEntity(response, responseCharset);
        
    }
    
    /**
     * HTTP Post
     * 
     * @param url
     *            请求的URL
     * @param content
     *            请求内容
     * @param contentType
     *            请求内容类型，HTTP Header中的<code>Content-type</code>
     * @param mimeType
     *            请求内容MIME类型
     * @param requestCharset
     *            请求内容字符集
     * @param responseCharset
     *            响应内容字符集
     * @return 响应内容实体
     * @throws IOException
     * @throws ClientProtocolException
     */
    private String post(String url, String content, String contentType,
            String mimeType, String requestCharset, String responseCharset)
            throws ClientProtocolException, IOException
    {
        CloseableHttpClient client = HttpClientBuilder.create().build();
        
        HttpPost post = new HttpPost(url);
        post.setHeader("Content-Type", contentType);
        ContentType type = ContentType.create(mimeType, requestCharset);
        HttpEntity requestEntity = new StringEntity(content, type);
        post.setEntity(requestEntity);
        HttpResponse response = client.execute(post);
        return consumeResponseEntity(response, responseCharset);
        
    }
    
    /**
     * HTTP Post
     * 
     * @param url
     *            请求URL
     * @param params
     *            请求参数
     * @param paramEncoding
     *            请求参数编码
     * @param responseCharset
     *            响应内容字符集
     * @return 响应内容实体
     * @throws IOException
     * @throws ClientProtocolException
     */
    private String post(String url, Map<String, String> params,
            String paramEncoding, String responseCharset)
            throws ClientProtocolException, IOException
    {
        CloseableHttpClient client = HttpClientBuilder.create().build();
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
        HttpResponse response = client.execute(post);
        return consumeResponseEntity(response, responseCharset);
    }
    
    /**
     * HTTP Post XML
     * 
     * @param url
     *            请求的URL
     * @param xml
     *            XML格式请求内容
     * @param requestCharset
     *            请求内容字符集
     * @param responseCharset
     *            响应内容字符集
     * @return 响应内容实体
     * @throws IOException
     * @throws ClientProtocolException
     */
    private String postXml(String url, String xml, String requestCharset,
            String responseCharset) throws ClientProtocolException, IOException
    {
        return post(url,
                xml,
                "text/xml; charset=" + requestCharset,
                "text/xml",
                requestCharset,
                responseCharset);
    }
    
    public String postSoapXml(String url, String soapXml, String action)
            throws IOException
    {
        URL u = new URL(url);
        
        URLConnection connection = u.openConnection();
        HttpURLConnection httpconn = (HttpURLConnection) connection;
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        bout.write(soapXml.getBytes("GBK"));
        //如果您的系统是utf-8,这里请改成bout.write(xml.getBytes("GBK"));
        byte[] b = bout.toByteArray();
        httpconn.setRequestProperty("Content-Length", String.valueOf(b.length));
        httpconn.setRequestProperty("Content-Type", "text/xml; charset=gb2312");
        httpconn.setRequestProperty("SOAPAction", action);
        httpconn.setRequestMethod("POST");
        httpconn.setDoInput(true);
        httpconn.setDoOutput(true);
        OutputStream out = httpconn.getOutputStream();
        out.write(b);
        out.close();
        InputStreamReader isr = new InputStreamReader(
                httpconn.getInputStream());
        BufferedReader in = new BufferedReader(isr);
        String inputLine;
        StringBuilder sb = new StringBuilder();
        while (null != (inputLine = in.readLine()))
        {
            sb.append(inputLine);
        }
        return sb.toString();
    }
    
    /**
     * HTTP Post JSON
     * 
     * @param url
     *            请求的URL
     * @param json
     *            JSON格式请求内容
     * @param requestCharset
     *            请求内容字符集
     * @param responseCharset
     *            响应内容字符集
     * @return 响应内容实体
     * @throws IOException
     * @throws ClientProtocolException
     */
    private String postJson(String url, String json, String requestCharset,
            String responseCharset) throws ClientProtocolException, IOException
    {
        return post(url,
                json,
                "application/json; charset=" + requestCharset,
                "application/json",
                requestCharset,
                responseCharset);
    }
    
    private String sslGet(String url, CloseableHttpClient sslClient,
            String authString) throws ClientProtocolException, IOException
    {
        HttpGet getMethod = new HttpGet(url);
        if (authString != null)
        {
            getMethod.setHeader("Authorization", authString);
        }
        HttpResponse response = sslClient.execute(getMethod);
        return consumeResponseEntity(response, getCharset());
        
    }
    
    /**
     * SSL Post
     * 
     * @param url
     *            请求的URL
     * @param content
     *            请求内容
     * @param contentType
     *            请求内容类型，HTTP Header中的<code>Content-type</code>
     * @param mimeType
     *            请求内容MIME类型
     * @param requestCharset
     *            请求内容字符集
     * @param responseCharset
     *            响应内容字符集
     * @param sch
     *            Scheme
     * @param authString
     *            头部信息中的<code>Authorization</code>
     * @return 响应内容实体
     * @throws IOException
     * @throws ClientProtocolException
     */
    private String sslPost(String url, String content, String contentType,
            String mimeType, String requestCharset, String responseCharset,
            CloseableHttpClient client, String authString)
            throws ClientProtocolException, IOException
    {
        HttpPost post = new HttpPost(url);
        if (authString != null)
            post.setHeader("Authorization", authString);
        if (contentType != null)
            post.setHeader("Content-Type", contentType);
        ContentType type = ContentType.create(mimeType, requestCharset);
        HttpEntity requestEntity = new StringEntity(content, type);
        post.setEntity(requestEntity);
        
        HttpResponse response = client.execute(post);
        return consumeResponseEntity(response, responseCharset);
        
    }
    
    /**
     * 安全的消耗（获取）响应内容实体
     * <p/>
     * 使用 {@link EntityUtils} 将响应内容实体转换为字符串，同时关闭输入流
     * <p/>
     * //TODO 响应内容太长不适宜使用 EntityUtils
     * 
     * @param response
     *            HttpResponse
     * @param responseCharset
     *            响应内容字符集
     * @return 响应内容实体
     * @throws IOException
     *             IOException
     */
    private String consumeResponseEntity(HttpResponse response,
            String responseCharset) throws IOException
    {
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
        {
            HttpEntity responseEntity = response.getEntity();
            String responseBody = EntityUtils.toString(responseEntity,
                    responseCharset);
            return responseBody;
        }
        else
        {
        }
        return null;
    }
    
    private CloseableHttpClient getSslHttpClient(String keyType, String keyPath,
            String keyPassword) throws Exception
    {
        KeyStore trustStore = KeyStore.getInstance(keyType);
        FileInputStream instream = new FileInputStream(new File(keyPath));
        trustStore.load(instream, keyPassword.toCharArray());
        
        // 验证密钥源
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("sunx509");
        kmf.init(trustStore, keyPassword.toCharArray());
        
        // 同位体验证信任决策源
        TrustManager[] trustManagers = { new MyX509TrustManager() };
        
        // 初始化安全套接字
        SSLContext sslContext = SSLContexts.custom().build();
        sslContext.init(kmf.getKeyManagers(), trustManagers, null);
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                sslContext,
                SSLConnectionSocketFactory.STRICT_HOSTNAME_VERIFIER);
        HttpClientBuilder clientBuilder = HttpClients.custom();
        clientBuilder.setSSLSocketFactory(sslsf);
        clientBuilder.setSslcontext(sslContext);
        return clientBuilder.build();
    }
    
    public String getCharset()
    {
        return charset;
    }
    
    public void setCharset(String charset)
    {
        this.charset = charset;
    }
    
    public String getKeyStoreType()
    {
        return keyStoreType;
    }
    
    public void setKeyStoreType(String keyStoreType)
    {
        this.keyStoreType = keyStoreType;
    }
    
    public String getKeyStorePath()
    {
        return keyStorePath;
    }
    
    public void setKeyStorePath(String keyStorePath)
    {
        this.keyStorePath = keyStorePath;
    }
    
    public String getKeyPassword()
    {
        return keyPassword;
    }
    
    public void setKeyPassword(String keyPassword)
    {
        this.keyPassword = keyPassword;
    }
    
    class MyX509TrustManager implements X509TrustManager
    {
        
        public MyX509TrustManager() throws Exception
        {
            
        }
        
        /*
         * Delegate to the default trust manager.
         */
        public void checkClientTrusted(X509Certificate[] chain, String authType)
                throws CertificateException
        {
            
        }
        
        /*
         * Delegate to the default trust manager.
         */
        public void checkServerTrusted(X509Certificate[] chain, String authType)
                throws CertificateException
        {
            
        }
        
        /*
         * Merely pass this through.
         */
        public X509Certificate[] getAcceptedIssuers()
        {
            return new java.security.cert.X509Certificate[0];
        }
    }
    
    public static void main(String[] args) throws Exception
    {
        HttpClient im = new HttpClient();
        im.setCharset("UTF-8");
        //      im.setKeyPassword("123456");
        //      im.setKeyStorePath("E:/t.jks");
        //      im.setKeyStoreType("jks");
        //      String aa = im.sslGet("https://www.menchuang.org.cn", ""); 
        //      System.out.println(aa);
        //        Header host = new BasicHeader("Host", "market.cnal.com");
        //        Header agent = new BasicHeader("User-Agent",
        //                "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:36.0) Gecko/20100101 Firefox/36.0");
        //        String string = im.get("http://market.cnal.com/share/market/cj30.json",
        //                host,
        //                agent);
        //        Header host = new BasicHeader("Host", "www1.njcein.com.cn");
        //        Header referer = new BasicHeader("Referer",
        //                "http://www1.njcein.com.cn/njxxnew/xmxx/zbgg/default.aspx");
        //        Map<String, String> params = new HashMap<String, String>();
        //        params.put("drpBiaoDuanType", "0");
        //        params.put("txtProjectName", "门窗");
        String json = "{\"name\":\"张三\",\"sex\":1,\"phone\":\"13888888888\",\"unionId\":\"AAAAAAAAAAAAAAAAAAAAAA\",\"productId\":\"产品ID\",\"recommen\":[{\"unionId\":\"11111111\",\"name\":\"推荐人名称\",\"parentId\":\"000000\",\"level\":1},{\"unionId\":\"222222\",\"name\":\"推荐人名称\",\"parentId\":\"1111111\",\"level\":2},{\"unionId\":\"333333\",\"name\":\"推荐人名称\",\"parentId\":\"2222222\",\"level\":3}]}";
        String string = im.postJson(
                "http://127.0.0.1:8080/crm-manager/open/addCustomer.htm", json);
        System.out.println(string);
        
    }
}
