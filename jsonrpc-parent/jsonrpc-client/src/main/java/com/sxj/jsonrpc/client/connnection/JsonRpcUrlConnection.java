package com.sxj.jsonrpc.client.connnection;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.sxj.jsonrpc.core.exception.JsonRpcConnectionException;

public class JsonRpcUrlConnection implements JsonRpcConnection
{
    
    private URL url;
    
    private HttpURLConnection conn;
    
    private int _statusCode;
    
    private String _statusMessage;
    
    public JsonRpcUrlConnection(URL url, HttpURLConnection conn)
    {
        this.url = url;
        this.conn = conn;
    }
    
    public String readHeader(String key)
    {
        
        return conn.getHeaderField(key);
    }
    
    public void writeArgument(Map<String, String> argMap) throws IOException
    {
        // not ensure open
        PrintWriter pw = new PrintWriter(conn.getOutputStream());
        Iterator<Entry<String, String>> iter = argMap.entrySet().iterator();
        int index = 1;
        int size = argMap.size();
        while (iter.hasNext())
        {
            Entry<String, String> entry = iter.next();
            pw.print(entry.getKey());
            pw.print("=");
            pw.print(URLEncoder.encode(entry.getValue(), "UTF-8"));
            if (index++ < size)
                pw.print("&");
            
        }
        
    }
    
    public String readResponse() throws IOException
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    public void sendRequest() throws IOException
    {
        
        _statusCode = 500;
        
        try
        {
            _statusCode = conn.getResponseCode();
        }
        catch (Exception e)
        {
        }
        
        InputStream is = null;
        
        if (_statusCode != 200)
        {
            StringBuffer sb = new StringBuffer();
            int ch;
            
            try
            {
                is = conn.getInputStream();
                
                if (is != null)
                {
                    while ((ch = is.read()) >= 0)
                        sb.append((char) ch);
                    
                    is.close();
                }
                
                is = conn.getErrorStream();
                if (is != null)
                {
                    while ((ch = is.read()) >= 0)
                        sb.append((char) ch);
                }
                
                _statusMessage = sb.toString();
            }
            catch (FileNotFoundException e)
            {
                throw new JsonRpcConnectionException(
                        "JsonProxy cannot connect to '" + url, e);
            }
            catch (IOException e)
            {
                if (is == null)
                    throw new JsonRpcConnectionException(
                            _statusCode + ": " + e, e);
                else
                    throw new JsonRpcConnectionException(_statusCode + ": "
                            + sb, e);
            }
            
            if (is != null)
                is.close();
        }
    }
    
    public int getStatusCode()
    {
        return _statusCode;
    }
    
    public String getStatusMessage()
    {
        
        return _statusMessage;
    }
    
    public InputStream getInputStream() throws IOException
    {
        return conn.getInputStream();
    }
    
    public OutputStream getOutputStream() throws IOException
    {
        return conn.getOutputStream();
    }
    
}
