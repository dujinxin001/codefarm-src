package com.codefarm.birt.spring;

import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.birt.report.engine.api.HTMLRenderOption;
import org.eclipse.birt.report.engine.api.HTMLServerImageHandler;
import org.eclipse.birt.report.engine.api.IRenderOption;
import org.eclipse.birt.report.engine.api.RenderOption;

public class HtmlSingleFormatBirtView extends AbstractSingleFormatBirtView
{
    
    public HtmlSingleFormatBirtView()
    {
        setContentType("text/html");
    }
    
    @Override
    protected RenderOption renderReport(Map<String, Object> modelData,
            HttpServletRequest request, HttpServletResponse response,
            BirtViewResourcePathCallback resourcePathCallback,
            Map<String, Object> appContextValuesMap, String reportName,
            String format, IRenderOption options) throws Throwable
    {
        
        ServletContext sc = request.getServletContext();
        HTMLRenderOption htmlOptions = new HTMLRenderOption(options);
        htmlOptions.setOutputFormat("html");
        htmlOptions.setOutputStream(response.getOutputStream());
        htmlOptions.setImageHandler(new HTMLServerImageHandler());
        htmlOptions.setBaseImageURL(birtViewResourcePathCallback
                .baseImageUrl(sc, request, reportName));
        htmlOptions.setImageDirectory(birtViewResourcePathCallback
                .imageDirectory(sc, request, reportName));
        htmlOptions.setBaseURL(
                birtViewResourcePathCallback.baseUrl(sc, request, reportName));
                
        return htmlOptions;
    }
}
