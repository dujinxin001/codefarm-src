package com.sxj.poi.transformer;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hwpf.converter.PicturesManager;
import org.apache.poi.hwpf.usermodel.PictureType;
import org.apache.poi.xwpf.converter.core.IImageExtractor;
import org.apache.poi.xwpf.converter.core.IURIResolver;

public abstract class AbstractPictureExactor implements PicturesManager,
        IPictureExactor, IImageExtractor, IURIResolver
{
    public static final String LOCAL_FILE_SCHEME = "file";
    
    public static final String HTTP_SCHEME = "http";
    
    public abstract String getScheme();
    
    public abstract String getPath();
    
    @Override
    public final String savePicture(byte[] content, PictureType pictureType,
            String suggestedName, float widthInches, float heightInches)
    {
        String file = save(content,
                pictureType,
                suggestedName,
                widthInches,
                heightInches).toString();
        return buildUrlString(file);
    }
    
    private String buildUrlString(String uri)
    {
        try
        {
            return new URL(uri).toString();
        }
        catch (MalformedURLException e)
        {
        }
        if (StringUtils.isEmpty(getScheme()))
            throw new RuntimeException("scheme must not be empty");
        return getScheme() + "://" + getPath() + "/" + uri;
    }
    
    @Override
    public final String resolve(String uri)
    {
        return buildUrlString(uri);
    }
    
    @Override
    public final void extract(String imagePath, byte[] imageData)
            throws IOException
    {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
                imageData);
        BufferedImage image = ImageIO.read(byteArrayInputStream);
        byteArrayInputStream.close();
        save(imageData,
                PictureType.findMatchingType(imageData),
                imagePath,
                Float.parseFloat(String.valueOf(image.getWidth())),
                Float.parseFloat(String.valueOf(image.getHeight())));
    }
    
    public static void main(String... args) throws URISyntaxException
    {
        String str = "file:///C:/test";
        URI uri = new URI(str);
        
        File file = new File(uri);
        //        System.out.println(uri.getScheme());
        System.out.println(file.getAbsolutePath());
    }
}
