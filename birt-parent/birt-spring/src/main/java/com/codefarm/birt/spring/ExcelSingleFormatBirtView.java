package com.codefarm.birt.spring;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.birt.report.engine.api.EXCELRenderOption;
import org.eclipse.birt.report.engine.api.IRenderOption;
import org.eclipse.birt.report.engine.api.RenderOption;

public class ExcelSingleFormatBirtView extends AbstractSingleFormatBirtView
{
    
    public ExcelSingleFormatBirtView()
    {
        setContentType("application/vnd.ms-excel");
    }
    
    @Override
    protected RenderOption renderReport(Map<String, Object> map,
            HttpServletRequest request, HttpServletResponse response,
            BirtViewResourcePathCallback resourcePathCallback,
            Map<String, Object> appContextValuesMap, String reportName,
            String format, IRenderOption options) throws Throwable
    {
        String oName = reportName;
        if (oName.toLowerCase().endsWith(".rptdesign"))
        {
            oName = oName.replaceAll("(?i).rptdesign", "");
        }
        
        response.setHeader("Content-Disposition",
                "attachment; filename=" + oName + ".xls");
        EXCELRenderOption excelOptions = new EXCELRenderOption(options);
        excelOptions.setOutputFormat("xls");
        //        pdfOptions.setOption(IPDFRenderOption.PAGE_OVERFLOW,
        //                IPDFRenderOption.FIT_TO_PAGE_SIZE);
        
        excelOptions.setOutputStream(response.getOutputStream());
        return excelOptions;
    }
    
}
