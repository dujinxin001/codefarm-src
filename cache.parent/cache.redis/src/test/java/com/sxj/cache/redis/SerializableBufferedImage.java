package com.sxj.cache.redis;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.util.Hashtable;

public class SerializableBufferedImage extends BufferedImage
{
    
    public SerializableBufferedImage(ColorModel cm, WritableRaster raster,
            boolean isRasterPremultiplied, Hashtable<?, ?> properties)
    {
        super(cm, raster, isRasterPremultiplied, properties);
        // TODO Auto-generated constructor stub
    }
    
    public SerializableBufferedImage(int width, int height, int imageType,
            IndexColorModel cm)
    {
        super(width, height, imageType, cm);
        // TODO Auto-generated constructor stub
    }
    
    public SerializableBufferedImage(int width, int height, int imageType)
    {
        super(width, height, imageType);
        // TODO Auto-generated constructor stub
    }
    
}
