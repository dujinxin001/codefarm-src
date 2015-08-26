package com.sxj.poi.transformer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

import org.apache.poi.hwpf.usermodel.PictureType;
import org.junit.After;
import org.junit.Test;

import com.sxj.poi.transformer.impl.DocTransformer;

public class DocTransformerTest
{
    
    @After
    public void tearDown() throws Exception
    {
    }
    
    @Test
    public void testToHTML() throws FileNotFoundException, POITransformException
    {
        DocTransformer transformer = new DocTransformer();
        transformer.setPictureExactor(new LocalPictureExactor("c:\\test\\"));
        transformer.toHTML(
                new FileInputStream(
                        new File("D:\\scm-repository\\git\\sxj\\abc.doc")),
                new FileOutputStream(
                        new File("D:\\scm-repository\\git\\sxj\\abc.html")));
    }
    
    class LocalPictureExactor extends AbstractPictureExactor
    {
        private String path;
        
        public LocalPictureExactor(String path)
        {
            super();
            this.path = path;
        }
        
        @Override
        protected URL save(byte[] content, PictureType pictureType,
                String suggestedName, float widthInches, float heightInches)
        {
            try
            {
                FileOutputStream fos = new FileOutputStream(
                        new File(path + File.separator + suggestedName));
                fos.write(content);
                fos.close();
                return new URL(
                        "file://" + path + File.separator + suggestedName);
            }
            catch (FileNotFoundException e)
            {
                e.printStackTrace();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            return null;
        }
        
        public String getPath()
        {
            return path;
        }
        
        public void setPath(String path)
        {
            this.path = path;
        }
        
    }
    
}
