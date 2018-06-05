package com.codefarm.birt.spring;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class BirtReportController
{
    
    @RequestMapping("/reports/{report}.rpt")
    public ModelAndView toHTML(@PathVariable("report") String report)
    {
        return new ModelAndView(report + ".rpt", null);
    }
    
    @RequestMapping("/reports/{report}.xls")
    public ModelAndView toXls(@PathVariable("report") String report)
    {
        return new ModelAndView(report + ".xls", null);
    }
    
    @RequestMapping("/reports/{report}.pdf")
    public ModelAndView toPdf(@PathVariable("report") String report)
    {
        return new ModelAndView(report + ".pdf", null);
    }
    // 10104
    //    @RequestMapping("/orders/{orderId}.html")
    //    public ModelAndView customerReport(@PathVariable("orderId") String orderId)
    //    {
    //        
    //        Map<String, Object> modelData = new HashMap<String, Object>();
    //        modelData.put("order", orderId);
    //        return new ModelAndView("orderDetails", modelData);
    //    }
    //    
    //    @RequestMapping("/masterReport")
    //    public ModelAndView masterReport()
    //    {
    //        Map<String, Object> modelData = new HashMap<String, Object>();
    //        return new ModelAndView("masterReport", modelData);
    //    }
    //    
    //    @RequestMapping("/SubReports.xls")
    //    public ModelAndView subReports()
    //    {
    //        Map<String, Object> modelData = new HashMap<String, Object>();
    //        return new ModelAndView("SubReports", modelData);
    //    }
    //    
    //    @RequestMapping("/reports/{report}.xls")
    //    public ModelAndView toXls(@PathVariable("report") String report)
    //    {
    //        Map<String, Object> modelData = new HashMap<String, Object>();
    //        return new ModelAndView(report, modelData);
    //    }
    
}
