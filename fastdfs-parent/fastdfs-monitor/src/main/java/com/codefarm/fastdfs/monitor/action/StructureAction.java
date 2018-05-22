package com.codefarm.fastdfs.monitor.action;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;

import org.csource.fastdfs.ClientGlobal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.codefarm.fastdfs.monitor.service.MonitorService;
import com.codefarm.fastdfs.monitor.service.StructureService;
import com.codefarm.fastdfs.monitor.util.Tools;
import com.codefarm.fastdfs.monitor.vo.Group;
import com.codefarm.fastdfs.monitor.vo.Line;
import com.codefarm.fastdfs.monitor.vo.Storage;

@Controller
@RequestMapping("/structure")
public class StructureAction
{
    @Autowired
    private StructureService structureService;
    
    @Autowired
    private MonitorService monitorService;
    
    private static final Logger logger = LoggerFactory.getLogger(StructureAction.class);
    
    @RequestMapping("/netStructure")
    public ModelAndView netStructure() throws Exception
    {
        ModelAndView mv = new ModelAndView("structure/netStructure.jsp");
        try
        {
            mv.addObject("groupInfo", monitorService.listGroupInfo());
            mv.addObject("trucker", getTrackForStruct());
        }
        catch (IOException e)
        {
            logger.error("", e);
        }
        return mv;
        
    }
    
    @RequestMapping("/serverInfo")
    public ModelAndView serverInfo(String ip) throws Exception
    {
        ModelAndView mv = new ModelAndView("structure/serverInfo.jsp");
        if (ip.indexOf(":") >= 0)
        {
            String[] data = ip.split(":");
            ip = data[0];
        }
        List<Group> groups = monitorService.listGroupInfo();
        for (Group group : groups)
        {
            for (Storage storage : group.getStorageList())
            {
                if (storage.getIpAddr().equals(ip))
                {
                    mv.addObject("serverInfo", storage);
                }
            }
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Calendar calendar = Calendar.getInstance();
        mv.addObject("end", sdf.format(calendar.getTime()));
        calendar.add(Calendar.HOUR, -1);
        mv.addObject("start", sdf.format(calendar.getTime()));
        return mv;
    }
    
    @ResponseBody
    @RequestMapping("/getForperformanceByIp")
    public List<Line> getForperformanceByIp(String ip)
    {
        List<Line> storageList = structureService.listStorageTopLine(ip);
        return storageList;
    }
    
    @ResponseBody
    @RequestMapping("/storageInfoForFile")
    public List<Line> storageInfoForFile(String ip)
    {
        List<Line> storageList = new ArrayList<Line>();
        storageList = structureService.listStorageAboutFile(ip);
        return storageList;
    }
    
    private String getTrackForStruct()
    {
        String result = "";
        try
        {
            ClientGlobal.init(Tools.getClassPath() + "fdfs_client.conf");
            
        }
        catch (IOException e)
        {
            logger.error("", e);
        }
        catch (Exception e)
        {
            logger.error("", e);
        }
        String configFile = Tools.getClassPath() + "fdfs_client.conf";
        FileInputStream fis = null;
        InputStreamReader isr = null;
        Properties p = new Properties();
        try
        {
            fis = new FileInputStream(configFile);
            isr = new InputStreamReader(fis, "UTF-8");
            p.load(isr);
            fis.close();
            isr.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (fis != null)
            {
                try
                {
                    fis.close();
                }
                catch (IOException e)
                {
                    logger.error("", e);
                }
            }
            if (isr != null)
            {
                try
                {
                    isr.close();
                }
                catch (IOException e)
                {
                    logger.error("", e);
                }
            }
        }
        
        result = p.getProperty("tracker_server");
        return result;
        
    }
}
