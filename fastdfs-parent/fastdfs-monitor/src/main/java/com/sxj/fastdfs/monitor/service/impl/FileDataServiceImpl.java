package com.sxj.fastdfs.monitor.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.sxj.fastdfs.monitor.service.BaseService;
import com.sxj.fastdfs.monitor.service.FileDataService;
import com.sxj.fastdfs.monitor.vo.DownloadFileRecord;
import com.sxj.fastdfs.monitor.vo.Fdfs_file;

@Service
public class FileDataServiceImpl extends BaseService implements FileDataService
{
    
    @Override
    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public List<Fdfs_file> getFileListByGroupName(String groupName)
    {
        List<Fdfs_file> files = new ArrayList<Fdfs_file>();
        Session session = getSession();
        
        StringBuilder queryString = new StringBuilder(
                "from Fdfs_file as f where  f.file_id like '" + groupName
                        + "%'");
        Query query = session.createQuery(queryString.toString());
        files = query.list();
        
        return files;
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public DownloadFileRecord getDownloadFileRecordByIpAndFileId(String ip,
            String fileId)
    {
        DownloadFileRecord downloadFileRecord = new DownloadFileRecord();
        Session session = getSession();
        StringBuilder queryString = new StringBuilder(
                "  from DownloadFileRecord df where df.src_ip='" + ip
                        + "' and df.fileId='" + fileId + "'");
        Query query = session.createQuery(queryString.toString());
        List<DownloadFileRecord> list = query.list();
        if (list.isEmpty())
        {
            return null;
        }
        else
        {
            return list.get(0);
        }
        
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void saveDF(DownloadFileRecord downloadFileRecord)
    {
        Session session = getSession();
        session.saveOrUpdate(downloadFileRecord);
    }
}
