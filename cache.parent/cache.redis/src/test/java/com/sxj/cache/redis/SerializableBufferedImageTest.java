package com.sxj.cache.redis;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import org.junit.Test;

public class SerializableBufferedImageTest
{
    @Test
    public void test() throws FileNotFoundException, IOException
    {
        BufferedImage bi = new BufferedImage(10, 10,
                BufferedImage.TYPE_3BYTE_BGR);
        new ObjectOutputStream(new FileOutputStream(new File("D:\\a.jpg"))).writeObject(bi.getData()
                .getDataBuffer());
    }
}
