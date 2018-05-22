package com.codefarm.poi.transformer;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class XlsxToXls
{
    private String outFn;
    
    private File inpFn;
    
    public XlsxToXls(File inpFn)
    {
        this.outFn = inpFn + ".xls";
        this.inpFn = inpFn;
    }
    
    public void transfer() throws InvalidFormatException, IOException,
            IllegalAccessException, InvocationTargetException
    {
        InputStream in = new FileInputStream(inpFn);
        try
        {
            XSSFWorkbook wbIn = new XSSFWorkbook(in);
            File outF = new File(outFn);
            if (outF.exists())
            {
                outF.delete();
            }
            
            Workbook wbOut = new HSSFWorkbook();
            int sheetCnt = wbIn.getNumberOfSheets();
            for (int i = 0; i < sheetCnt; i++)
            {
                Sheet sIn = wbIn.getSheetAt(i);
                Sheet sOut = wbOut.createSheet(sIn.getSheetName());
                Iterator<Row> rowIt = sIn.rowIterator();
                while (rowIt.hasNext())
                {
                    Row rowIn = rowIt.next();
                    Row rowOut = sOut.createRow(rowIn.getRowNum());
                    
                    Iterator<Cell> cellIt = rowIn.cellIterator();
                    while (cellIt.hasNext())
                    {
                        Cell cellIn = cellIt.next();
                        Cell cellOut = rowOut.createCell(
                                cellIn.getColumnIndex(), cellIn.getCellType());
                                
                        switch (cellIn.getCellType())
                        {
                            case Cell.CELL_TYPE_BLANK:
                                break;
                                
                            case Cell.CELL_TYPE_BOOLEAN:
                                cellOut.setCellValue(
                                        cellIn.getBooleanCellValue());
                                break;
                                
                            case Cell.CELL_TYPE_ERROR:
                                cellOut.setCellValue(
                                        cellIn.getErrorCellValue());
                                break;
                                
                            case Cell.CELL_TYPE_FORMULA:
                                cellOut.setCellFormula(cellIn.getCellFormula());
                                break;
                                
                            case Cell.CELL_TYPE_NUMERIC:
                                cellOut.setCellValue(
                                        cellIn.getNumericCellValue());
                                break;
                                
                            case Cell.CELL_TYPE_STRING:
                                cellOut.setCellValue(
                                        cellIn.getStringCellValue());
                                break;
                        }
                        
                        {
                            CellStyle styleIn = cellIn.getCellStyle();
                            CellStyle styleOut = cellOut.getCellStyle();
                            BeanUtils.copyProperties(styleOut, styleIn);
                            styleOut.setDataFormat(styleIn.getDataFormat());
                        }
                        cellOut.setCellComment(cellIn.getCellComment());
                        
                    }
                }
            }
            OutputStream out = new BufferedOutputStream(
                    new FileOutputStream(outF));
            try
            {
                wbOut.write(out);
            }
            finally
            {
                out.close();
            }
        }
        finally
        {
            in.close();
        }
    }
    
    public static void main(String... args)
    {
        XlsxToXls to = new XlsxToXls(
                new File("D:\\scm-repository\\git\\sxj\\abc-2.xlsx"));
        try
        {
            to.transfer();
        }
        catch (InvalidFormatException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IllegalAccessException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (InvocationTargetException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}