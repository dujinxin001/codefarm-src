package com.sxj.fastdfs.monitor.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.sxj.fastdfs.monitor.service.BaseService;
import com.sxj.fastdfs.monitor.service.TestModuleService;
import com.sxj.fastdfs.monitor.vo.DownloadFileRecord;
import com.sxj.fastdfs.monitor.vo.Fdfs_file;
import com.sxj.fastdfs.monitor.vo.Line;

@Service
public class TestModuleServiceImpl extends BaseService implements
        TestModuleService
{
    
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public List<Fdfs_file> getAllFileList()
    {
        Session session = getSession();
        Query query = session.createQuery(" from Fdfs_file");
        return query.list();
        
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Fdfs_file getFileByFileId(String fileId)
    {
        Session session = getSession();
        Query query = session.createQuery(" from Fdfs_file f where f.file_id='"
                + fileId + "'");
        List<Fdfs_file> fileList = query.list();
        if (fileList.size() > 0)
        {
            return fileList.get(0);
        }
        else
        {
            return null;
        }
        
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void saveFastFile(Fdfs_file f)
    {
        Session session = getSession();
        session.saveOrUpdate(f);
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Map<String, Object[]> getAllFileListByTen(String ip)
    {
        
        Map<String, Object[]> map = new HashMap<String, Object[]>();
        Session session = getSession();
        Query query = session.createQuery(" from DownloadFileRecord  f where f.src_ip='"
                + ip + "'  order by f.accessCount desc");
        List<DownloadFileRecord> list = query.list();
        Line sc = new Line(ip);
        sc.setName(ip);
        String[] listName = new String[10];
        Line[] lines = new Line[1];
        long sum = 0;
        for (int i = 0; i < list.size(); i++)
        {
            DownloadFileRecord downloadFileRecord = list.get(i);
            if (i < 10)
            {
                
                Fdfs_file f = getFileByFileId(downloadFileRecord.getFileId()
                        .substring(1));
                sc.getData().add(new Object[] { f.getFile_name(),
                        downloadFileRecord.getAccessCount() });
                listName[i] = f.getFile_name();
                sum = sum + downloadFileRecord.getAccessCount();
            }
            else
            {
                sum = sum + downloadFileRecord.getAccessCount();
            }
        }
        lines[0] = sc;
        map.put("x", listName);
        map.put("y", lines);
        map.put("sum", new Object[] { sum });
        return map;
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Line getAllFileListForPie(String ip)
    {
        Session session = getSession();
        Query query = session.createQuery(" from DownloadFileRecord  f where f.src_ip='"
                + ip + "'  order by f.accessCount desc");
        query.setMaxResults(10);
        List<DownloadFileRecord> list = query.list();
        Line sc = new Line(ip);
        sc.setName(ip);
        for (DownloadFileRecord downloadFileRecord : list)
        {
            Fdfs_file f = getFileByFileId(downloadFileRecord.getFileId()
                    .substring(1));
            sc.getData().add(new Object[] { f.getFile_name(),
                    downloadFileRecord.getAccessCount() });
            
        }
        return sc;
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public List<Fdfs_file> getAllFileListByPage(String pageNum,
            String pageSize, String keyForSearch)
    {
        Session session = getSession();
        StringBuilder sb = new StringBuilder(" from Fdfs_file f");
        if (!StringUtils.isEmpty(keyForSearch))
        {
            sb.append("  where f.file_id='" + keyForSearch + "'");
            pageNum = "1";
        }
        Query query = session.createQuery(sb.toString());
        query.setMaxResults(Integer.parseInt(pageSize));
        query.setFirstResult((Integer.parseInt(pageNum) - 1)
                * Integer.parseInt(pageSize));
        return query.list();
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public int getCountDownLoadFile(String keyForSearch)
    {
        Session session = getSession();
        StringBuilder sb = new StringBuilder(" from Fdfs_file f");
        if (keyForSearch != null && keyForSearch != "")
        {
            sb.append("  where f.file_id='" + keyForSearch + "'");
        }
        Query query = session.createQuery(sb.toString());
        return query.list().size();
    }
    
}
