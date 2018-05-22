package com.codefarm.fastdfs.monitor.service;


/**
 * Created with IntelliJ IDEA.
 * User: devuser
 * Date: 12-8-28
 * Time: 下午5:04
 * To change this template use File | Settings | File Templates.
 */
public interface JobService
{
    void updateGroupByMinute() throws Exception;
    
    void updateGroupByHour() throws Exception;
    
    void updateGroupByDay() throws Exception;
    
    void clearJobData() throws Exception;
}
