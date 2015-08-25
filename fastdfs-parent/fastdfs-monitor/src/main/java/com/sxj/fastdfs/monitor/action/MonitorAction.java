package com.sxj.fastdfs.monitor.action;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.servlet.ServletContext;

import org.csource.common.MyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.sxj.fastdfs.monitor.service.MonitorService;
import com.sxj.fastdfs.monitor.vo.Group;
import com.sxj.fastdfs.monitor.vo.Line;

@Controller
@RequestMapping("/monitor")
public class MonitorAction implements ServletContextAware
{
    private ServletContext servletContext;
    
    public void setServletContext(ServletContext servletContext)
    { // 实现接口中的setServletContext方法
        this.servletContext = servletContext;
    }
    
    @Autowired
    private MonitorService monitorService;
    
    @ResponseBody
    @RequestMapping("/listGroupInfo")
    public List<Group> listStorageInfo() throws Exception
    {
        return monitorService.listGroupInfo();
    }
    
    /**
     * 性能监控---实时获取 节点信息 和 cpu以及内存使用率
     * @return
     * @throws Exception 
     * @throws MyException
     */
    @RequestMapping("/performance")
    public ModelAndView performance() throws Exception
    {
        ModelAndView mv = new ModelAndView("monitor/performance.jsp");
        List<Group> groups = monitorService.listGroupInfo();
        
        mv.addObject("groups", groups);
        
        return mv;
    }
    
    /**
     * 性能监控---根据组名读数据库tbstorage获取CPU和内存使用率的曲线
     * @param groupName
     * @return
     * @throws IOException
     * @throws MyException
     */
    @ResponseBody
    @RequestMapping("/getPerformanceLine")
    public List<Line> getPerformanceLine(String groupName) throws IOException
    {
        return monitorService.listStorageLines(groupName);
    }
    
    /**
     * 流量监控---获取节点信息和时间段
     * @return
     * @throws Exception 
     * @throws MyException
     */
    @RequestMapping("/netTraffic")
    public ModelAndView netTraffic() throws Exception
    {
        ModelAndView mv = new ModelAndView("monitor/netTraffic.jsp");
        mv.addObject("groupInfo", monitorService.listGroupInfo());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Calendar calendar = Calendar.getInstance();
        mv.addObject("end", sdf.format(calendar.getTime()));
        calendar.add(Calendar.HOUR, -1);
        mv.addObject("start", sdf.format(calendar.getTime()));
        return mv;
    }
    
    /**
     * 流量监控---读取tbstorage或tbstoragehour或tbstorageday获取
     * @param ip
     * @param start
     * @param end
     * @return
     * @throws IOException
     * @throws MyException
     */
    @ResponseBody
    @RequestMapping("/getNetTrafficLine")
    public List<Line> getNetTrafficLine(String ip, String start, String end)
            throws IOException
    {
        return monitorService.getNetTrafficLines(ip, start, end);
    }
    
    /**
     * 容量监控---获取组信息
     * @return
     * @throws Exception 
     * @throws MyException
     */
    @RequestMapping("/capacity")
    public ModelAndView capacity() throws Exception
    {
        ModelAndView mv = new ModelAndView("monitor/capacity.jsp");
        mv.addObject("groupInfo", monitorService.listGroupInfo());
        
        return mv;
    }
    
    /**
     * 容量监控----容量历史---读取tbstoragehour获取最后一小时的数据
     * @param ip
     * @return
     * @throws IOException
     * @throws MyException
     */
    @RequestMapping("/storageInfo")
    public ModelAndView storageInfo(String ip) throws IOException
    {
        ModelAndView mv = new ModelAndView("monitor/storageInfo.jsp");
        mv.addObject("storage", monitorService.getStorageByIp(ip));
        return mv;
    }
    
    /**
     * 容量监控----容量历史---查询tbstoragehour获取容量图
     * @param ip
     * @param startTime
     * @param endTime
     * @return
     * @throws IOException
     * @throws MyException
     */
    @ResponseBody
    @RequestMapping("/capactityStorage")
    public List<Line> capactityStorage(String ip, String startTime,
            String endTime) throws IOException
    {
        System.out.println(ip);
        List<Line> result = new ArrayList<Line>();
        result.add(monitorService.getListStoragesInfo(ip, startTime, endTime));
        return result;
    }
    
    /**
     * 容量监控----容量历史---查询tbstoragehour获取文件数量图
     * @param ip
     * @param startTime
     * @param endTime
     * @return
     * @throws IOException
     * @throws MyException
     */
    @ResponseBody
    @RequestMapping("/fileCountStorage")
    public List<Line> fileCountStorage(String ip, String startTime,
            String endTime) throws IOException
    {
        List<Line> result = new ArrayList<Line>();
        result = monitorService.getListFileCountStorage(ip, startTime, endTime);
        return result;
    }
    
    @RequestMapping("/testUpload")
    public ModelAndView testUpload() throws IOException
    {
        ModelAndView mv = new ModelAndView("monitor/testUpload.jsp");
        return mv;
    }
    
    @RequestMapping(value = "/oneFileUpload", method = RequestMethod.POST)
    public ModelAndView handleFormUpload(@RequestParam("name") String name,
            @RequestParam("file") MultipartFile file) throws IOException
    {
        
        ModelAndView mv = new ModelAndView("main/index.jsp");
        System.out.println("name：" + name);
        System.out.println("上传文件：" + file.getOriginalFilename());
        String f = null;
        
        return mv;
    }
}
