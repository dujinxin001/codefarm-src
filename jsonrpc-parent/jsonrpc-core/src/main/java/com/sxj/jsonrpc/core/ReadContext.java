package com.sxj.jsonrpc.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.WeakHashMap;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sxj.jsonrpc.core.exception.StreamEndedException;

public class ReadContext
{
    
    private InputStream input;
    
    private ObjectMapper mapper;
    
    private static final WeakHashMap<InputStream, ReadContext> contexts = new WeakHashMap<InputStream, ReadContext>();
    
    public synchronized static ReadContext getReadContext(InputStream ips,
            ObjectMapper mapper) throws JsonParseException, IOException
    {
        ReadContext ret = contexts.get(ips);
        if (ret == null)
        {
            ret = new ReadContext(ips, mapper);
        }
        return ret;
    }
    
    public ReadContext(InputStream ips, ObjectMapper mapper)
            throws JsonParseException, IOException
    {
        this.input = new NoCloseInputStream(ips);
        this.mapper = mapper;
    }
    
    public JsonNode nextValue() throws IOException
    {
        return mapper.readValue(input, JsonNode.class);
    }
    
    public void assertReadable() throws StreamEndedException, IOException
    {
        if (input.markSupported())
        {
            input.mark(1);
            if (input.read() == -1)
            {
                throw new StreamEndedException();
            }
            input.reset();
        }
    }
    
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((input == null) ? 0 : input.hashCode());
        result = prime * result + ((mapper == null) ? 0 : mapper.hashCode());
        return result;
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ReadContext other = (ReadContext) obj;
        if (input == null)
        {
            if (other.input != null)
                return false;
        }
        else if (!input.equals(other.input))
            return false;
        if (mapper == null)
        {
            if (other.mapper != null)
                return false;
        }
        else if (!mapper.equals(other.mapper))
            return false;
        return true;
    }
    
}
