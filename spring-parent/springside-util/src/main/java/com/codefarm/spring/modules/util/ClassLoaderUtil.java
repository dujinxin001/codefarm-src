package com.codefarm.spring.modules.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;

public class ClassLoaderUtil
{
    public static InputStream getResourceAsStream(String configFile) throws IOException
    {
        //        InputStream configStream = ClassLoaderUtil.class.getClassLoader()
        //                .getParent()
        //                .getResourceAsStream(configFile);
        //        if (configStream == null)
        //            configStream = ClassLoaderUtil.class.getClassLoader()
        //                    .getResourceAsStream(configFile);
        //        if (configStream == null)
        //            configStream = Thread.currentThread()
        //                    .getContextClassLoader()
        //                    .getResourceAsStream(configFile);
        //        configStream = getResource(ClassLoaderUtil.class.getClassLoader(),
        //                configFile);
        
        InputStream configStream = ResourcePatternUtils
                .getResourcePatternResolver(
                        new PathMatchingResourcePatternResolver())
                .getResource(configFile)
                .getInputStream();
        if (configStream == null)
            throw new FileNotFoundException(
                    "Cannot find "
                            + ClassLoaderUtil.class.getClassLoader()
                                    .getResource(configFile)
                                    .toExternalForm()
                            + " !!!");
        return configStream;
    }
    
}
