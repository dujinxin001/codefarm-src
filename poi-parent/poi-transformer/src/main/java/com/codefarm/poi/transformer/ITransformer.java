package com.codefarm.poi.transformer;

import java.io.InputStream;
import java.io.OutputStream;

public interface ITransformer
{
    public void toHTML(InputStream source, OutputStream output)
            throws POITransformException;
            
    public void toPDF(InputStream source, OutputStream output)
            throws POITransformException;
            
    public void setPictureExactor(AbstractPictureExactor pictureExactor);
}
