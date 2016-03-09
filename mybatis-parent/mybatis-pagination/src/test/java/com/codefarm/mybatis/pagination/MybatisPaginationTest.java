package com.codefarm.mybatis.pagination;

import java.io.IOException;
import java.io.Reader;
import java.util.List;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Test;

import com.codefarm.mybatis.pagination.entity.User;
import com.codefarm.mybatis.pagination.po.PageUserDTO;

public class MybatisPaginationTest
{
    
    @Test
    public void test() throws IOException
    {
        String resource = "com/sxj/mybatis/pagination/Configuration.xml";
        Reader reader = Resources.getResourceAsReader(resource);
        SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(reader);
        SqlSession session1 = factory.openSession(true);
        
        //        User parameter = new User();
        //        List<User> users = session1.selectList("users.selectUsers", parameter);
        //        System.out.println("返回结果共有:" + users.size());
        //        for (User user : users)
        //        {
        //            System.out.println(user.getName());
        //        }
        PageUserDTO page = new PageUserDTO();
        page.setPagable(true);
        page.setCurrentPage(1);
        page.setShowCount(1);
        List<User> users = session1.selectList("users.selectUsers", page);
        System.out.println("分页返回结果共有:" + users.size());
        for (User user : users)
        {
            System.out.println(user.getName());
        }
    }
    
}
