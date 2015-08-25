package com.sxj.fastdfs.monitor.service;

import java.io.IOException;
import java.util.List;

import com.sxj.fastdfs.monitor.vo.User;

/**
 * Created with IntelliJ IDEA.
 * User: wanglt
 * Date: 12-8-31
 * Time: 上午9:43
 * To change this template use File | Settings | File Templates.
 */

public interface UserService
{
    List<User> userlist(String username) throws IOException;
    
    void updateOrSaveUser(User user) throws IOException;
    
    User findById(String id) throws IOException;
    
    void delUser(String id) throws IOException;
    
    boolean login(String name, String password) throws IOException;
    
    User findByName(String name) throws IOException;
}
