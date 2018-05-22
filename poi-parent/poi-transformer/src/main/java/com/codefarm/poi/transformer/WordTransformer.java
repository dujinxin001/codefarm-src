package com.codefarm.poi.transformer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.codefarm.poi.transformer.impl.DocTransformer;
import com.codefarm.poi.transformer.impl.DocXTransformer;

public class WordTransformer implements ITransformer
{
    private ITransformer docTransformer = new DocTransformer();
    
    private ITransformer docxTransformer = new DocXTransformer();
    
    private AbstractPictureExactor pictureExactor;
    
    @Override
    public void toHTML(InputStream source, OutputStream output)
            throws POITransformException
    {
        docTransformer.setPictureExactor(pictureExactor);
        docxTransformer.setPictureExactor(pictureExactor);
        NoCloseInputStream wordInputStream = new NoCloseInputStream(source);
        try
        {
            wordInputStream.mark(0);
            docTransformer.toHTML(wordInputStream, output);
        }
        catch (POITransformException e)
        {
            try
            {
                wordInputStream.reset();
            }
            catch (IOException e1)
            {
                e1.printStackTrace();
            }
            docxTransformer.toHTML(wordInputStream, output);
        }
        finally
        {
            try
            {
                wordInputStream.closeNow();
                output.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
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
        this.pictureExactor = pictureExactor;
    }
    
}
