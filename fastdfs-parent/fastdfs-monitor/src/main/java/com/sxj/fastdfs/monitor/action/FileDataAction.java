package com.sxj.fastdfs.monitor.action;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.sxj.fastdfs.monitor.service.FileDataService;
import com.sxj.fastdfs.monitor.service.MonitorService;
import com.sxj.fastdfs.monitor.vo.Fdfs_file;
import com.sxj.fastdfs.monitor.vo.FileSize;
import com.sxj.fastdfs.monitor.vo.Group;

@Controller
@RequestMapping("/dataStructure")
public class FileDataAction
{
    @Autowired
    private MonitorService monitorService;
    
    @Autowired
    private FileDataService fileDataService;
    
    @RequestMapping("/fileData")
    public ModelAndView fileData() throws IOException
    {
        ModelAndView mv = new ModelAndView("dataStructure/fileData.jsp");
        List<Group> list = monitorService.listGroups();
        Map<String, FileSize> container = new HashMap<String, FileSize>();
        for (Group group : list)
        {
            List<Fdfs_file> files = fileDataService.getFileListByGroupName(group.getGroupName());
            FileSize sizes = new FileSize();
            for (Fdfs_file file : files)
            {
                if (file.getFileSize() >= 0 && file.getFileSize() < 30 * 1024)
                {
                    sizes.setMiniSmall(sizes.getMiniSmall() + 1);
                }
                else if (file.getFileSize() >= 30 * 1024
                        && file.getFileSize() < 100 * 1024)
                {
                    sizes.setSmall(sizes.getSmall() + 1);
                }
                else if (file.getFileSize() >= 100 * 1014
                        && file.getFileSize() < 500 * 1024)
                {
                    sizes.setMiddle(sizes.getMiddle() + 1);
                }
                else
                {
                    sizes.setLarge(sizes.getLarge() + 1);
                }
            }
            container.put(group.getGroupName(), sizes);
        }
        mv.addObject("fileYdm", list);
        mv.addObject("fileSizes", container);
        return mv;
    }
    
}
