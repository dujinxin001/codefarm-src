package com.codefarm.poi.transformer;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

public class NoCloseInputStream extends BufferedInputStream
{
    private InputStream in;
    
    public NoCloseInputStream(InputStream in, int size)
    {
        super(in, size);
        this.in = in;
    }
    
    public NoCloseInputStream(InputStream in)
    {
        super(in);
        this.in = in;
    }
    
    @Override
    public void close() throws IOException
    {
    }
    
    public void closeNow() throws IOException
    {
        super.close();
    }
    
}
