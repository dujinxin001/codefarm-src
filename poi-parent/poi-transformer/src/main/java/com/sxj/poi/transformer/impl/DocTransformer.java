package com.sxj.poi.transformer.impl;

import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.poi.hwpf.converter.WordToHtmlConverter;
import org.apache.poi.hwpf.converter.WordToHtmlUtils;
import org.w3c.dom.Document;

import com.sxj.poi.transformer.AbstractPictureExactor;
import com.sxj.poi.transformer.ITransformer;
import com.sxj.poi.transformer.POITransformException;

public class DocTransformer implements ITransformer
{
    
    private AbstractPictureExactor pictureExactor;
    
    public void toHTML(InputStream source, OutputStream output)
            throws POITransformException
    {
        
        try
        {
            WordToHtmlConverter wordToHtmlConverter = new WordToHtmlConverter(
                    DocumentBuilderFactory.newInstance()
                            .newDocumentBuilder()
                            .newDocument());
            if (pictureExactor != null)
                wordToHtmlConverter.setPicturesManager(pictureExactor);
            wordToHtmlConverter
                    .processDocument(WordToHtmlUtils.loadDoc(source));
            Document htmlDocument = wordToHtmlConverter.getDocument();
            DOMSource domSource = new DOMSource(htmlDocument);
            StreamResult streamResult = new StreamResult(output);
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer serializer = tf.newTransformer();
            serializer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            serializer.setOutputProperty(OutputKeys.INDENT, "yes");
            serializer.setOutputProperty(OutputKeys.METHOD, "html");
            serializer.transform(domSource, streamResult);
        }
        catch (Exception e)
        {
            throw new POITransformException(e);
        }
        
    }
    
    public void toPDF(InputStream source, OutputStream output)
            throws POITransformException
    {
        // TODO Auto-generated method stub
        
    }
    
    public void setPictureExactor(AbstractPictureExactor pictureExactor)
    {
        this.pictureExactor = pictureExactor;
    }
    
}
