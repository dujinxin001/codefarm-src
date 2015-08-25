package com.sxj.jsonrpc.client.connnection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

public interface JsonRpcConnection
{
    
    /**
     * 从response中读取指定的header
     * @param key
     * @return
     */
    public String readHeader(String key);
    
    /**
     * 将参数写入
     * @param argMap
     * @throws IOException 
     */
    public void writeArgument(Map<String, String> argMap) throws IOException;
    
    /**读取response内容
     * @return
     */
    public String readResponse() throws IOException;
    
    public void sendRequest() throws IOException;
    
    /**
     * 获取状态码
     * @return
     */
    public int getStatusCode();
    
    public String getStatusMessage();
    
    /**
     * 获取inputstream，json反序列时直接从inputstream读避免二次拷贝
     * @return
     * @throws IOException
     */
    public InputStream getInputStream() throws IOException;
    
    /**
     * 获取outputstream
     * @return
     * @throws IOException
     */
    public OutputStream getOutputStream() throws IOException;
}
