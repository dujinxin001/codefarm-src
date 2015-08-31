package com.sxj.poi.transformer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import org.junit.After;
import org.junit.Test;

import com.sxj.poi.transformer.impl.XlsTransformer;

public class ExcelTransformerTest
{
    
    @After
    public void tearDown() throws Exception
    {
    }
    
    public void xlsTransformerTest()
            throws FileNotFoundException, POITransformException
    {
        XlsTransformer transformer = new XlsTransformer();
        FileInputStream source = new FileInputStream(
                new File("D:\\scm-repository\\git\\sxj\\abc.xls"));
        FileOutputStream output = new FileOutputStream(
                new File("D:\\scm-repository\\git\\sxj\\abc.xls.html"));
        transformer.toHTML(source, output);
    }
    
    @Test
    public void testExcelTransformer()
            throws FileNotFoundException, POITransformException
    {
        FileInputStream xls = new FileInputStream(
                new File("D:\\scm-repository\\git\\sxj\\abc.xls"));
        FileOutputStream xlsHTML = new FileOutputStream(
                new File("D:\\scm-repository\\git\\sxj\\abc.xls.html"));
        FileInputStream xlsx = new FileInputStream(
                new File("D:\\scm-repository\\git\\sxj\\abc.xlsx"));
        FileOutputStream xlsxHTML = new FileOutputStream(
                new File("D:\\scm-repository\\git\\sxj\\abc.xlsx.html"));
        ExcelTransformer transformer = new ExcelTransformer();
        transformer.setOutputHTMLTag(true);
        transformer.toHTML(xls, xlsHTML);
        transformer.toHTML(xlsx, xlsxHTML);
    }
    
}
