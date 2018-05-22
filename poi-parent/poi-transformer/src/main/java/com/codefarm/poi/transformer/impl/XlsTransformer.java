package com.codefarm.poi.transformer.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.poi.hssf.converter.ExcelToHtmlConverter;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.w3c.dom.Document;

import com.codefarm.poi.transformer.AbstractPictureExactor;
import com.codefarm.poi.transformer.ITransformer;
import com.codefarm.poi.transformer.POITransformException;

public class XlsTransformer implements ITransformer
{
    
    public void toHTML(InputStream source, OutputStream output)
            throws POITransformException
    {
        HSSFWorkbook workbook;
        try
        {
            workbook = new HSSFWorkbook(source);
            
            ExcelToHtmlConverter excelToHtmlConverter = new ExcelToHtmlConverter(
                    DocumentBuilderFactory.newInstance()
                            .newDocumentBuilder()
                            .newDocument());
            excelToHtmlConverter.processWorkbook(workbook);
            excelToHtmlConverter.setOutputRowNumbers(false);
            Document doc = excelToHtmlConverter.getDocument();
            DOMSource domSource = new DOMSource(doc);
            StreamResult streamResult = new StreamResult(output);
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer serializer = tf.newTransformer();
            // TODO set encoding from a command argument
            serializer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            serializer.setOutputProperty(OutputKeys.INDENT, "no");
            serializer.setOutputProperty(OutputKeys.METHOD, "html");
            serializer.transform(domSource, streamResult);
        }
        catch (Exception e)
        {
            throw new POITransformException(e);
        }
        finally
        {
            try
            {
                source.close();
                output.close();
            }
            catch (IOException e)
            {
            }
            
        }
    }
    
    public void toPDF(InputStream source, OutputStream output)
            throws POITransformException
    {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void setPictureExactor(AbstractPictureExactor pictureExactor)
    {
        // TODO Auto-generated method stub
        
    }
    
}
