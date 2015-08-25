package com.sxj.birt.spring;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.birt.report.engine.api.IPDFRenderOption;
import org.eclipse.birt.report.engine.api.IRenderOption;
import org.eclipse.birt.report.engine.api.PDFRenderOption;
import org.eclipse.birt.report.engine.api.RenderOption;

public class PdfSingleFormatBirtView extends AbstractSingleFormatBirtView
{
    
    public PdfSingleFormatBirtView()
    {
        setContentType("application/pdf");
        
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
        
        response.setHeader("Content-Disposition", "attachment; filename="
                + oName + ".pdf");
        
        PDFRenderOption pdfOptions = new PDFRenderOption(options);
        pdfOptions.setOutputFormat("pdf");
        pdfOptions.setOption(IPDFRenderOption.PAGE_OVERFLOW,
                IPDFRenderOption.FIT_TO_PAGE_SIZE);
        pdfOptions.setOutputStream(response.getOutputStream());
        return pdfOptions;
    }
}
