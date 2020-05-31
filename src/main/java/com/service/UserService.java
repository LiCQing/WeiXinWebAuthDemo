package com.service;

import com.dao.UserDao;
import com.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    Logger log = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserDao userDao;

    public User getUserByOpenId(String openid){
        log.error("获取用户" + openid);
        return userDao.getUserByOpenId(openid);
    }

    public void updateUser(User user){
        log.error("修改用户，绑定openid");
        userDao.updateUser(user);
    }

    public List<User> getListUser(){
       return userDao.getListUser();
    }


    public User loginOrRegister(User user) throws Exception {
        user.checkNull();
        User userByName = userDao.getUserByName(user.getUserName());
        if(userByName == null){
            userDao.insertUser(user);
            return user;
        }
        if(!userByName.getPassWord().equals(user.getPassWord())){
            throw new Exception("密码不正确");
        }
        return userByName;
    }
}
