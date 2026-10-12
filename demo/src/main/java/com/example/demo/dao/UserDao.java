package com.example.demo.dao;

import com.example.demo.dto.UserRegisterRequest;
import com.example.demo.model.User;

public interface UserDao {
    Integer createUser(UserRegisterRequest userRegisterRequest);
    User getUserById(Integer userId);
}
