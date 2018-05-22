package com.codefarm.poi.transformer.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.poi.xwpf.converter.xhtml.XHTMLConverter;
import org.apache.poi.xwpf.converter.xhtml.XHTMLOptions;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import com.codefarm.poi.transformer.AbstractPictureExactor;
import com.codefarm.poi.transformer.ITransformer;
import com.codefarm.poi.transformer.POITransformException;

public class DocXTransformer implements ITransformer
{
    private AbstractPictureExactor exactor;
    
    @Override
    public void toHTML(InputStream source, OutputStream output)
            throws POITransformException
    {
        try
        {
            XWPFDocument document = new XWPFDocument(source);
            XHTMLOptions options = XHTMLOptions.create();
            if (exactor != null)
            {
                options.setExtractor(exactor);
                options.URIResolver(exactor);
            }
            XHTMLConverter.getInstance().convert(document, output, options);
        }
        catch (IOException e)
        {
            throw new POITransformException(e);
        }
    }
    
    @Override
    public void toPDF(InputStream source, OutputStream output)
            throws POITransformException
    {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void setPictureExactor(AbstractPictureExactor pictureExactor)
    {
        exactor = pictureExactor;
    }
    
}
