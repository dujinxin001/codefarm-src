package com.codefarm.fastdfs.monitor.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.codefarm.fastdfs.monitor.service.BaseService;
import com.codefarm.fastdfs.monitor.service.StructureService;
import com.codefarm.fastdfs.monitor.vo.Line;
import com.codefarm.fastdfs.monitor.vo.Storage;

@Service
public class StructureServiceImpl extends BaseService implements
        StructureService
{
    @Override
    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public List<Line> listStorageTopLine(String ip)
    {
        List<Line> lines = new ArrayList<Line>();
        Session session = getSession();
        Query query = session.createQuery("from Storage s where s.ipAddr=:ip order by s.created desc");
        List<Storage> results = query.setString("ip", ip)
                .setMaxResults(10)
                .list();
        Line line = new Line(ip);
        for (int i = results.size() - 1; i >= 0; i--)
        {
            Storage ss = results.get(i);
            line.getData().add(new Object[] { ss.getCreated().getTime(),
                    ss.getMem() });
        }
        lines.add(line);
        return lines;
        
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    public List<Line> listStorageAboutFile(String ip)
    {
        List<Line> lines = new ArrayList<Line>();
        Session session = getSession();
        Query query = session.createQuery("from Storage s where s.ipAddr=:ip order by s.created desc");
        List<Storage> results = query.setString("ip", ip)
                .setMaxResults(10)
                .list();
        Line line = new Line(ip);
        for (int i = results.size() - 1; i >= 0; i--)
        {
            Storage ss = results.get(i);
            line.getData().add(new Object[] { ss.getCreated().getTime(),
                    ss.getTotalDownloadCount() });
        }
        lines.add(line);
        Line line1 = new Line(ip);
        for (int i = results.size() - 1; i >= 0; i--)
        {
            Storage ss = results.get(i);
            line1.getData().add(new Object[] { ss.getCreated().getTime(),
                    ss.getTotalUploadCount() });
        }
        lines.add(line1);
        return lines;
    }
    
}
