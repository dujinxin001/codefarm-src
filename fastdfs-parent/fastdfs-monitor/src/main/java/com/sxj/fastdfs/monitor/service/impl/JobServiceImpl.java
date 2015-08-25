package com.sxj.fastdfs.monitor.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.ProtoCommon;
import org.csource.fastdfs.StructGroupStat;
import org.csource.fastdfs.StructStorageStat;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.sxj.fastdfs.monitor.dao.FdfsDao;
import com.sxj.fastdfs.monitor.service.BaseService;
import com.sxj.fastdfs.monitor.service.FileDataService;
import com.sxj.fastdfs.monitor.service.JobService;
import com.sxj.fastdfs.monitor.service.WarningService;
import com.sxj.fastdfs.monitor.util.BuildMail;
import com.sxj.fastdfs.monitor.util.JsshProxy;
import com.sxj.fastdfs.monitor.util.Tools;
import com.sxj.fastdfs.monitor.vo.Group;
import com.sxj.fastdfs.monitor.vo.GroupDay;
import com.sxj.fastdfs.monitor.vo.GroupHour;
import com.sxj.fastdfs.monitor.vo.Machine;
import com.sxj.fastdfs.monitor.vo.Storage;
import com.sxj.fastdfs.monitor.vo.StorageDay;
import com.sxj.fastdfs.monitor.vo.StorageHour;
import com.sxj.fastdfs.monitor.vo.WarningData;
import com.sxj.fastdfs.monitor.vo.WarningUser;

@Service
public class JobServiceImpl extends BaseService implements JobService
{
    
    private static final Logger logger = LoggerFactory.getLogger(JobServiceImpl.class);
    
    private static final Logger clearlogger = LoggerFactory.getLogger("clear-log");
    
    @Autowired
    private WarningService warningService;
    
    @Autowired
    private FileDataService fileDataService;
    
    @Autowired
    private FdfsDao fdfsDao;
    
    Map<String, Date> datemap = new HashMap<String, Date>();
    
    @Override
    @Scheduled(cron = "0 0/1 * * * ?")
    @Transactional(propagation = Propagation.REQUIRED)
    public void updateGroupByMinute() throws Exception
    {
        logger.info("group minute data upate begin...");
        List<Group> groups = getGroupInfoByMinute();
        
        Session session = getSession();
        for (Group group : groups)
        {
            session.save(group);
        }
        logger.info("group minute data upated end");
    }
    
    @Override
    @Scheduled(cron = "0 0 0/1 * * ?")
    @Transactional(propagation = Propagation.REQUIRED)
    public void updateGroupByHour() throws Exception
    {
        logger.info("group hour data upate begin...");
        List<GroupHour> groups = getGroupInfoByHour();
        Session session = getSession();
        for (GroupHour group : groups)
        {
            session.save(group);
        }
        logger.info("group hour data upated end");
    }
    
    @Override
    @Scheduled(cron = "0 0 0 0/1 * ?")
    @Transactional(propagation = Propagation.REQUIRED)
    public void updateGroupByDay() throws Exception
    {
        logger.info("group day data upate begin...");
        List<GroupDay> groups = getGroupInfoByDay();
        Session session = getSession();
        for (GroupDay group : groups)
        {
            session.save(group);
        }
        logger.info("group day data upated end");
    }
    
    @Override
    @Scheduled(cron = "0 0 0/1 * * ?")
    @Transactional(propagation = Propagation.REQUIRED)
    public void clearJobData() throws Exception
    {
        clearlogger.info("clearJobData  begin");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -10);
        Date stime = calendar.getTime();
        //        logger.info("clearJobData  stime"+stime);
        //        String hql = "delete Group as g,Storage s  where g.id = s.  g.created < ?";
        //        Session session = getSession();
        //        Query query = session.createQuery(hql);
        //        query.setCalendar(0,calendar);
        //        int i = query.executeUpdate();
        //        session.beginTransaction().commit();
        int g = fdfsDao.deleteGroup(stime);
        int gd = fdfsDao.deleteGroupDay(stime);
        int gh = fdfsDao.deleteGroupHour(stime);
        int s = fdfsDao.deleteStorage(stime);
        int sd = fdfsDao.deleteStorageDay(stime);
        int sh = fdfsDao.deleteStorageHour(stime);
        clearlogger.info("clearJobData  time=" + stime + "   end g = " + g
                + " gd = " + gd + " gh = " + gh + " s=" + s + " sd=" + sd
                + " sh=" + sh);
    }
    
    private List<Group> getGroupInfoByMinute() throws Exception
    {
        
        List<Group> result = new ArrayList<Group>();
        // noinspection ConstantConditions
        ClientGlobal.init(Tools.getClassPath() + "fdfs_client.conf");
        //        logger.info("network_timeout=" + ClientGlobal.g_network_timeout + "ms");
        //        logger.info("charset=" + ClientGlobal.g_charset);
        TrackerClient tracker = new TrackerClient();
        TrackerServer trackerServer = tracker.getConnection();
        if (trackerServer == null)
        {
            return result;
        }
        StructGroupStat[] groupStats = tracker.listGroups(trackerServer);
        if (groupStats == null)
        {
            logger.error("ERROR! list groups error, error no: "
                    + tracker.getErrorCode());
            return result;
        }
        //        logger.info("group count: " + groupStats.length);
        for (StructGroupStat groupStat : groupStats)
        {
            Group group = new Group();
            BeanUtils.copyProperties(groupStat, group);
            StructStorageStat[] storageStats = tracker.listStorages(trackerServer,
                    groupStat.getGroupName());
            for (StructStorageStat storageStat : storageStats)
            {
                
                Storage storage = new Storage();
                
                BeanUtils.copyProperties(storageStat, storage);
                storage.setId(null);
                //                System.out.println("getGroupInfoByMinute: storageId:"+storage.getId());
                storage.setCurStatus(ProtoCommon.getStorageStatusCaption(storageStat.getStatus()));
                
                storage.setGroup(group);
                storage.setGroupName(group.getGroupName());
                group.getStorageList().add(storage);
            }
            result.add(group);
        }
        
        Date date = new Date();
        String cmd = "ps -aux|grep fdfs";
        for (Machine machine : Tools.machines)
        {
            List<String> strList = new ArrayList<String>();
            if (machine.isConfigType())
                strList = Tools.exeRemoteConsole(machine.getIp(),
                        machine.getUsername(),
                        machine.getPassword(),
                        cmd);
            else
                strList = new JsshProxy(machine.getIp(), machine.getUsername(),
                        machine.getPort(), machine.getSsh()).execute(cmd)
                        .getExecuteLines();
            for (String str : strList)
            {
                if (str.contains("storage.conf"))
                {
                    for (Group group : result)
                    {
                        group.setCreated(date);
                        for (Storage storage : group.getStorageList())
                        {
                            
                            if (machine.getIp()
                                    .equalsIgnoreCase(storage.getIpAddr()))
                            {
                                String[] strArrray = str.replaceAll(" +", ",")
                                        .split(",");
                                storage.setCpu(strArrray[2]);
                                storage.setMem(Float.parseFloat(strArrray[3]));
                                storage.setCreated(date);
                                //warning
                                warning(storage);
                                
                            }
                            warningOffline(storage);
                        }
                    }
                }
            }
        }
        
        return result;
    }
    
    private List<GroupHour> getGroupInfoByHour() throws Exception
    {
        List<GroupHour> result = new ArrayList<GroupHour>();
        // noinspection ConstantConditions
        
        ClientGlobal.init(Tools.getClassPath() + "fdfs_client.conf");
        //        logger.info("network_timeout=" + ClientGlobal.g_network_timeout + "ms");
        //        logger.info("charset=" + ClientGlobal.g_charset);
        TrackerClient tracker = new TrackerClient();
        TrackerServer trackerServer = tracker.getConnection();
        if (trackerServer == null)
        {
            return result;
        }
        StructGroupStat[] groupStats = tracker.listGroups(trackerServer);
        if (groupStats == null)
        {
            logger.error("ERROR! list groups error, error no: "
                    + tracker.getErrorCode());
            return result;
        }
        //        logger.info("group count: " + groupStats.length);
        for (StructGroupStat groupStat : groupStats)
        {
            GroupHour group = new GroupHour();
            BeanUtils.copyProperties(groupStat, group);
            StructStorageStat[] storageStats = tracker.listStorages(trackerServer,
                    groupStat.getGroupName());
            for (StructStorageStat storageStat : storageStats)
            {
                StorageHour storage = new StorageHour();
                BeanUtils.copyProperties(storageStat, storage);
                storage.setCurStatus(ProtoCommon.getStorageStatusCaption(storageStat.getStatus()));
                storage.setId(null);
                //                System.out.println("getGroupInfoByHour: storageId:"+storage.getId());
                storage.setGroup(group);
                storage.setGroupName(group.getGroupName());
                group.getStorageList().add(storage);
            }
            result.add(group);
        }
        Date date = new Date();
        String cmd = "ps -aux|grep fdfs";
        for (Machine machine : Tools.machines)
        {
            List<String> strList = new ArrayList<String>();
            if (machine.isConfigType())
                strList = Tools.exeRemoteConsole(machine.getIp(),
                        machine.getUsername(),
                        machine.getPassword(),
                        cmd);
            else
                strList = new JsshProxy(machine.getIp(), machine.getUsername(),
                        machine.getPort(), machine.getSsh()).execute(cmd)
                        .getExecuteLines();
            for (String str : strList)
            {
                if (str.contains("storage.conf"))
                {
                    for (GroupHour group : result)
                    {
                        group.setCreated(date);
                        for (StorageHour storage : group.getStorageList())
                        {
                            if (machine.getIp()
                                    .equalsIgnoreCase(storage.getIpAddr()))
                            {
                                String[] strArrray = str.replaceAll(" +", ",")
                                        .split(",");
                                storage.setCpu(strArrray[2]);
                                storage.setMem(Float.parseFloat(strArrray[3]));
                                storage.setCreated(date);
                            }
                        }
                    }
                }
            }
        }
        return result;
    }
    
    private List<GroupDay> getGroupInfoByDay() throws Exception
    {
        List<GroupDay> result = new ArrayList<GroupDay>();
        // noinspection ConstantConditions
        
        ClientGlobal.init(Tools.getClassPath() + "fdfs_client.conf");
        //        logger.info("network_timeout=" + ClientGlobal.g_network_timeout + "ms");
        //        logger.info("charset=" + ClientGlobal.g_charset);
        TrackerClient tracker = new TrackerClient();
        TrackerServer trackerServer = tracker.getConnection();
        if (trackerServer == null)
        {
            return result;
        }
        StructGroupStat[] groupStats = tracker.listGroups(trackerServer);
        if (groupStats == null)
        {
            logger.error("ERROR! list groups error, error no: "
                    + tracker.getErrorCode());
            return result;
        }
        //        logger.info("group count: " + groupStats.length);
        for (StructGroupStat groupStat : groupStats)
        {
            GroupDay group = new GroupDay();
            BeanUtils.copyProperties(groupStat, group);
            StructStorageStat[] storageStats = tracker.listStorages(trackerServer,
                    groupStat.getGroupName());
            for (StructStorageStat storageStat : storageStats)
            {
                StorageDay storage = new StorageDay();
                BeanUtils.copyProperties(storageStat, storage);
                
                storage.setCurStatus(ProtoCommon.getStorageStatusCaption(storageStat.getStatus()));
                storage.setGroup(group);
                storage.setId(null);
                storage.setGroupName(group.getGroupName());
                //                System.out.println("getGroupInfoByDay: storageId:"+storage.getId());
                group.getStorageList().add(storage);
            }
            result.add(group);
        }
        Date date = new Date();
        String cmd = "ps -aux|grep fdfs";
        for (Machine machine : Tools.machines)
        {
            List<String> strList = new ArrayList<String>();
            if (machine.isConfigType())
                strList = Tools.exeRemoteConsole(machine.getIp(),
                        machine.getUsername(),
                        machine.getPassword(),
                        cmd);
            else
                strList = new JsshProxy(machine.getIp(), machine.getUsername(),
                        machine.getPort(), machine.getSsh()).execute(cmd)
                        .getExecuteLines();
            for (String str : strList)
            {
                if (str.contains("storage.conf"))
                {
                    for (GroupDay group : result)
                    {
                        group.setCreated(date);
                        for (StorageDay storage : group.getStorageList())
                        {
                            if (machine.getIp()
                                    .equalsIgnoreCase(storage.getIpAddr()))
                            {
                                String[] strArrray = str.replaceAll(" +", ",")
                                        .split(",");
                                storage.setCpu(strArrray[2]);
                                storage.setMem(Float.parseFloat(strArrray[3]));
                                storage.setCreated(date);
                            }
                        }
                    }
                }
            }
        }
        return result;
    }
    
    private void warning(Storage storage) throws IOException
    {
        List<WarningData> warningDatas = warningService.findByIp(storage.getIpAddr());
        StringBuffer stringBuffer = new StringBuffer("异常服务器："
                + storage.getIpAddr() + "</br>");
        if (!warningDatas.isEmpty())
        {
            float wdCup = Float.parseFloat(warningDatas.get(0).getWdCpu());
            float wdMem = warningDatas.get(0).getWdMem();
            long wdFreeMB = warningDatas.get(0).getWdFreeMB();
            boolean res = true;
            if (Float.parseFloat(storage.getCpu()) > wdCup)
            {
                stringBuffer.append("cpu使用率当前值为： " + storage.getCpu()
                        + "% 大于预警值：" + wdCup + "%</br>");
                res = false;
            }
            if (storage.getMem() > wdMem)
            {
                stringBuffer.append("内存使用率当前值为： " + storage.getMem()
                        + "% 大于预警值：" + wdMem + "%</br>");
                res = false;
            }
            if (storage.getFreeMB() < wdFreeMB)
            {
                stringBuffer.append("可用空间当前值为： " + storage.getFreeMB()
                        + "MB 小于预警值：" + wdFreeMB + "MB</br>");
                res = false;
            }
            if (!res)
            {
                BuildMail buildMail = new BuildMail();
                List<WarningUser> warningUser = new ArrayList<WarningUser>();
                warningUser = warningService.findWarUser();
                for (WarningUser wu : warningUser)
                {
                    buildMail.sendWarning("汽车之家FDFS监控",
                            wu.getEmail(),
                            "fdfs预警报告",
                            stringBuffer.toString());
                }
            }
        }
    }
    
    public void warningOffline(Storage storage) throws IOException
    {
        List<WarningData> warningDatas = warningService.findByIp(storage.getIpAddr());
        boolean res = false;
        StringBuffer stringBuffer = new StringBuffer("异常服务器 ："
                + storage.getIpAddr() + "</br>");
        if (storage.getCurStatus().equals("OFFLINE"))
        {
            stringBuffer.append("服务器停止工作");
            if (datemap.containsKey(storage.getIpAddr()))
            {
                Date offdate = datemap.get(storage.getIpAddr());
                Date now = new Date();
                long temp = now.getTime() - offdate.getTime();
                if (temp >= 1000 * 60 * 60)
                {
                    datemap.put(storage.getIpAddr(), new Date());
                    res = true;
                }
            }
            else
            {
                datemap.put(storage.getIpAddr(), new Date());
                res = true;
            }
        }
        else
        {
            if (datemap.containsKey(storage.getIpAddr()))
            {
                datemap.remove(storage.getIpAddr());
            }
        }
        if (res)
        {
            BuildMail buildMail = new BuildMail();
            List<WarningUser> warningUser = new ArrayList<WarningUser>();
            warningUser = warningService.findWarUser();
            for (WarningUser wu : warningUser)
            {
                buildMail.sendWarning("汽车之家FDFS监控",
                        wu.getEmail(),
                        "fdfs预警报告",
                        stringBuffer.toString());
            }
        }
    }
    
}
