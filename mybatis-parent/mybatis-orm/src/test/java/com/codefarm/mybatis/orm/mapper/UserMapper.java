package com.codefarm.mybatis.orm.mapper;

import java.util.List;

import com.codefarm.mybatis.orm.annotations.Criteria;
import com.codefarm.mybatis.orm.annotations.Delete;
import com.codefarm.mybatis.orm.annotations.Insert;
import com.codefarm.mybatis.orm.annotations.Select;
import com.codefarm.mybatis.orm.annotations.Update;
import com.codefarm.mybatis.orm.model.TestUser;
import com.codefarm.mybatis.orm.po.TestUserCriterias;

/**
 * 注意接口方法名称必须唯一，用作MappedStatementId
 * @author zhangjian
 *
 */
public interface UserMapper
{
    
    @Insert
    int insertUser(TestUser user);
    
    @Insert
    int insert(TestUser user);
    
    @Insert
    int insertUserList(List<TestUser> users);
    
    @Insert
    int insertUserArray(TestUser[] users);
    
    /**
     * UserMapper.xml
     * @param users
     * @return
     */
    int insertABC(TestUser[] users);
    
    @Update
    int updateSingle(TestUser user);
    
    @Update
    int update(TestUser newUser,
            @Criteria(column = "username") String username);
    
    @Update
    int updateUser(TestUser user);
    
    @Update
    int updateUsers(TestUser newUser, TestUserCriterias criterias);
    
    @Select(orderby = "userid desc")
    TestUser getById(@Criteria(column = "userid") Long id);
    
    @Select
    List<TestUser> select(@Criteria(column = "userid") Long id,
            @Criteria(column = "username") String username);
    
    @Select
    List<TestUser> selectByCriteriasAndUsername(TestUserCriterias criterias,
            @Criteria(column = "username") String username);
    
    @Select(orderby = "userid desc")
    List<TestUser> selectByIds(@Criteria(column = "userid") Long[] ids);
    
    @Select(orderby = "userid asc")
    List<TestUser> selectByCriterias(TestUserCriterias criterias);
    
    /**
     * UserMapper.xml
     * @param criterias
     * @param username
     * @return
     */
    List<TestUser> selectABC(TestUserCriterias criterias, String username);
    
    @Delete
    int delete(@Criteria(column = "userid") Long id);
    
    @Delete
    int deleteByIdAndName(@Criteria(column = "userid") Long id,
            @Criteria(column = "username") String name);
    
    @Delete
    int deleteByCriterias(TestUserCriterias criterias);
    
    @Delete
    int deleteByCriteriasAndUserName(TestUserCriterias criterias,
            @Criteria(column = "username") String username);
    
}
