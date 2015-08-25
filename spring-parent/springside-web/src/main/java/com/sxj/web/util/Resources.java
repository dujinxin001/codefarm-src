package com.sxj.web.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletContext;

public final class Resources
{
    
    private Resources()
    {
        /**
         * Intentionally blank to force static usage
         */
    }
    
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        throw new CloneNotSupportedException();
    }
    
    public static InputStream getResourceStream(final String path,
            final ServletContext servletContext, final Class<?> clazz)
            throws IOException
    {
        InputStream is = clazz.getClassLoader().getResourceAsStream(path);
        if (is == null)
        {
            is = Thread.currentThread()
                    .getContextClassLoader()
                    .getResourceAsStream(path);
        }
        if (is == null)
        {
            String fileName = servletContext.getRealPath(path);
            File file = new File(fileName);
            if (file.exists())
            {
                is = new FileInputStream(fileName);
            }
        }
        if (is == null)
        {
            File file = new File(path);
            if (file.exists())
            {
                is = new FileInputStream(path);
            }
        }
        
        if (is == null)
        {
            throw new IOException(
                    String.format("unable to locate resource - %s", path));
        }
        return is;
    }
    
}
