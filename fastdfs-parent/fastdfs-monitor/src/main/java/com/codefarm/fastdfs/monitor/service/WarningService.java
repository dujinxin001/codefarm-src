package com.codefarm.fastdfs.monitor.service;

import java.io.IOException;
import java.util.List;

import com.codefarm.fastdfs.monitor.vo.PageInfo;
import com.codefarm.fastdfs.monitor.vo.WarningData;
import com.codefarm.fastdfs.monitor.vo.WarningUser;

/**
 * Created with IntelliJ IDEA.
 * User: wanglt
 * Date: 12-8-28
 * Time: 上午10:37
 * To change this template use File | Settings | File Templates.
 */
public interface WarningService
{
    void updateWarning(WarningData wd) throws IOException;
    
    List<WarningData> findWarning() throws IOException;
    
    List<WarningData> findWarning(WarningData wd, PageInfo pageInfo)
            throws IOException;
    
    WarningData findById(String id) throws IOException;
    
    void delWarning(String id) throws IOException;
    
    List<WarningData> findByIp(String ip) throws IOException;
    
    List<WarningUser> findWarUser() throws IOException;
    
    List<WarningUser> findWarUser(WarningUser wu, PageInfo pageInfo)
            throws IOException;
    
    WarningUser findUserId(String id) throws IOException;
    
    void delWarUser(String id) throws IOException;
    
    void updateWarUser(WarningUser wu) throws IOException;
}
