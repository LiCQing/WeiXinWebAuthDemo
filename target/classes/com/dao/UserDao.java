package com.dao;

import com.model.User;

import java.util.List;

public interface UserDao {

     User getUserByName(String name);

     void insertUser(User user);

     User getUserByOpenId(String openId);

     User getUserById(int id);

     void updateUser(User user);

    List<User> getListUser();
}
