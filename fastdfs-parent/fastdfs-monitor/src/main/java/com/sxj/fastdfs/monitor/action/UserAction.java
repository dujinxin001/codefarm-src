package com.sxj.fastdfs.monitor.action;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.sxj.fastdfs.monitor.service.UserService;
import com.sxj.fastdfs.monitor.vo.Message;
import com.sxj.fastdfs.monitor.vo.User;

@Controller
@RequestMapping("/user")
public class UserAction
{
    @Autowired
    private UserService userService;
    
    @RequestMapping("/userlist")
    public ModelAndView userlist(String username) throws IOException
    {
        ModelAndView mv = new ModelAndView("user/userlist.jsp");
        List<User> userlist = userService.userlist(username);
        mv.addObject("userlist", userlist);
        return mv;
    }
    
    @RequestMapping("/useradd")
    public ModelAndView useradd(String id) throws IOException
    {
        ModelAndView mv = new ModelAndView("user/useradd.jsp");
        if (!StringUtils.isEmpty(id))
        {
            User user = userService.findById(id);
            mv.addObject("id", user.getId());
            mv.addObject("name", user.getName());
            mv.addObject("psword", user.getPsword());
            mv.addObject("power", user.getPower());
        }
        
        return mv;
    }
    
    @ResponseBody
    @RequestMapping("/saveuser")
    public Message saveWarning(String name, String psword, String power)
            throws IOException
    {
        Message message = null;
        User userbyname = userService.findByName(name);
        if (userbyname == null)
        {
            String result = "添加成功";
            User user = new User();
            user.setName(name);
            user.setPsword(psword);
            user.setPower(power);
            userService.updateOrSaveUser(user);
            message = new Message();
            message.setStatusCode("200");
            message.setMessage(result);
            
        }
        else
        {
            String result = "用户名重复";
            message = new Message();
            message.setStatusCode("300");
            message.setMessage(result);
        }
        return message;
    }
    
    @ResponseBody
    @RequestMapping("/deluser")
    public Message deluser(String ids) throws IOException
    {
        Message message = null;
        String[] id = ids.split(",");
        for (String i : id)
        {
            userService.delUser(i);
        }
        message = new Message();
        message.setStatusCode("200");
        message.setMessage("删除成功");
        return message;
    }
}
