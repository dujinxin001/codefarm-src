package com.sxj.poi.transformer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.hwpf.usermodel.PictureType;
import org.junit.After;
import org.junit.Test;

import com.sxj.poi.transformer.impl.DocTransformer;
import com.sxj.poi.transformer.impl.DocXTransformer;

public class DocTransformerTest
{
    
    @After
    public void tearDown() throws Exception
    {
    }
    
    public void testToHTML() throws FileNotFoundException, POITransformException
    {
        DocTransformer transformer = new DocTransformer();
        transformer.setPictureExactor(
                new LocalPictureExactor("c:\\test\\", "file"));
        transformer.toHTML(
                new FileInputStream(
                        new File("D:\\scm-repository\\git\\sxj\\abc.doc")),
                new FileOutputStream(
                        new File("D:\\scm-repository\\git\\sxj\\abc.html")));
    }
    
    @Test
    public void testDocxToTHML()
            throws FileNotFoundException, POITransformException
    {
        DocXTransformer transformer = new DocXTransformer();
        transformer.setPictureExactor(
                new LocalPictureExactor("c:\\test\\", "file"));
        transformer.toHTML(
                new FileInputStream(
                        new File("D:\\scm-repository\\git\\sxj\\abc.docx")),
                new FileOutputStream(
                        new File("D:\\scm-repository\\git\\sxj\\def.html")));
    }
    
    class LocalPictureExactor extends AbstractPictureExactor
    {
        private String path;
        
        private String scheme;
        
        public LocalPictureExactor(String path, String scheme)
        {
            super();
            this.path = path;
            this.scheme = scheme;
        }
        
        @Override
        public String save(byte[] content, PictureType pictureType,
                String suggestedName, float widthInches, float heightInches)
        {
            try
            {
                FileOutputStream fos = new FileOutputStream(
                        new File(path + File.separator + suggestedName));
                fos.write(content);
                fos.close();
                return suggestedName;
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
        
        public String getScheme()
        {
            return scheme;
        }
        
        public void setScheme(String scheme)
        {
            this.scheme = scheme;
        }
        
    }
    
}
