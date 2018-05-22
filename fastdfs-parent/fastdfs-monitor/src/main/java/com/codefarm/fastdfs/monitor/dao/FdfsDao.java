package com.codefarm.fastdfs.monitor.dao;

import java.util.Date;

/**
 * Created by zhufeng on 15-1-15.
 */
public interface FdfsDao {
    int deleteGroup(Date date);
    int deleteGroupDay(Date date);
    int deleteGroupHour(Date date);
    int deleteStorage(Date date);
    int deleteStorageDay(Date date);
    int deleteStorageHour(Date date);
}
