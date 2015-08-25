package com.sxj.fastdfs.monitor.action;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient1;
import org.csource.fastdfs.StorageServer;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.sxj.fastdfs.monitor.service.MonitorService;
import com.sxj.fastdfs.monitor.service.TestModuleService;
import com.sxj.fastdfs.monitor.util.Tools;
import com.sxj.fastdfs.monitor.vo.Fdfs_file;
import com.sxj.fastdfs.monitor.vo.Group;
import com.sxj.fastdfs.monitor.vo.Line;
import com.sxj.fastdfs.monitor.vo.Message;

@Controller
@RequestMapping("/testModule")
public class TestModuleAction
{
    @Autowired
    private TestModuleService testModuleService;
    
    @Autowired
    private MonitorService monitorService;
    
    private static final Logger logger = LoggerFactory.getLogger(TestModuleAction.class);
    
    @RequestMapping("/testDownLoad")
    public ModelAndView testDownLoad(String pageNum, String pageSize,
            String keyForSearch)
    {
        ModelAndView mv = new ModelAndView("testModule/downLoadTest.jsp");
        List<Fdfs_file> list = testModuleService.getAllFileListByPage(pageNum,
                pageSize,
                keyForSearch);
        int countDownLoadFile = testModuleService.getCountDownLoadFile(keyForSearch);
        mv.addObject("testFileCount", countDownLoadFile);
        if (!StringUtils.isEmpty(keyForSearch))
        {
            mv.addObject("pageNum", "1");
        }
        else
        {
            mv.addObject("pageNum", pageNum);
        }
        mv.addObject("pageSize", pageSize);
        mv.addObject("testFileList", list);
        mv.addObject("keySearch", keyForSearch);
        return mv;
    }
    
    @ResponseBody
    @RequestMapping("/toDownLoadToLocal")
    public Message toDownLoadToLocal(HttpServletResponse response,
            String fileId, String srcIpAddr, String fileName) throws Exception
    {
        Message message = null;
        String conf_filename = Thread.currentThread()
                .getContextClassLoader()
                .getResource("fdfs_client.conf")
                .getPath();
        try
        {
            ClientGlobal.init(conf_filename);
            
            System.out.println("network_timeout="
                    + ClientGlobal.g_network_timeout + "ms");
            System.out.println("charset=" + ClientGlobal.g_charset);
            TrackerClient tracker = new TrackerClient();
            TrackerServer trackerServer = tracker.getConnection();
            StorageServer storageServer = null;
            StorageClient1 client = new StorageClient1(trackerServer,
                    storageServer);
            byte[] bytes = client.download_file1(fileId);
            response.setHeader("content-disposition", "attachment;filename="
                    + fileName);
            if (bytes != null)
            {
                OutputStream os = response.getOutputStream();
                os.write(bytes);
                os.close();
                Fdfs_file f = testModuleService.getFileByFileId(fileId);
                if (f != null)
                {
                    
                    testModuleService.saveFastFile(f);
                }
            }
        }
        catch (IOException e)
        {
            logger.error("", e);
        }
        return message;
    }
    
    @RequestMapping("/accessFile")
    public ModelAndView accessFile() throws Exception
    {
        ModelAndView mv = new ModelAndView("testModule/accessFileCharts.jsp");
        List<Group> groups = monitorService.listGroupInfo();
        mv.addObject("groups", groups);
        
        return mv;
    }
    
    @ResponseBody
    @RequestMapping("/tenFileDownLoad")
    public Map<String, Object[]> tenFileDownLoad(String ip)
    {
        Map<String, Object[]> map = new HashMap<String, Object[]>();
        map = testModuleService.getAllFileListByTen(ip);
        return map;
    }
    
    @ResponseBody
    @RequestMapping("/allFilePie")
    public List<Line> allFilePie(String ip)
    {
        Line line = testModuleService.getAllFileListForPie(ip);
        List<Line> fileList = new ArrayList<Line>();
        fileList.add(line);
        return fileList;
    }
    
    @RequestMapping("/downloadByApi")
    public void downloadByApi(String fieldId, String fileName,
            HttpServletResponse response) throws Exception
    {
        
        ClientGlobal.init(Tools.getClassPath() + "fdfs_client.conf");
        logger.info("network_timeout=" + ClientGlobal.g_network_timeout + "ms");
        logger.info("charset=" + ClientGlobal.g_charset);
        TrackerClient tracker = new TrackerClient();
        TrackerServer trackerServer = tracker.getConnection();
        if (trackerServer == null)
        {
            return;
        }
        
        StorageClient1 client = new StorageClient1(trackerServer, null);
        byte[] bytes = client.download_file1(fieldId);
        
        logger.info("length:" + bytes.length);
        
        response.setHeader("Content-disposition", "attachment; filename="
                + fileName);
        OutputStream os = response.getOutputStream();
        os.write(bytes);
        os.close();
    }
}
