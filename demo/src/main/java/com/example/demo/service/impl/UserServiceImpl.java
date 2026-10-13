package com.example.demo.service.impl;

import com.example.demo.dao.UserDao;
import com.example.demo.dto.UserRegisterRequest;
import com.example.demo.dto.UserLoginRequest;
import com.example.demo.model.User;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Component
public class UserServiceImpl implements UserService {
    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserDao userDao;

    @Override
    public Integer register(UserRegisterRequest userRegisterRequest) {

        User user = userDao.getUserByEmail(userRegisterRequest.getEmail());
        if (user != null) {
            log.warn("註冊失敗：Email {} 已被註冊", userRegisterRequest.getEmail());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already registered");
        } else {
            String hashedPassword = DigestUtils.md5DigestAsHex(
                    userRegisterRequest.getPassword().getBytes(StandardCharsets.UTF_8));
            UserRegisterRequest hashedRequest = new UserRegisterRequest();
            hashedRequest.setEmail(userRegisterRequest.getEmail());
            hashedRequest.setPassword(hashedPassword);
            return userDao.createUser(hashedRequest);
        }
    }

    @Override
    public User getUserById(Integer userId) {

        return userDao.getUserById(userId);
    }

    @Override
    public User login(UserLoginRequest userLoginRequest) {
        User user = userDao.getUserByEmail(userLoginRequest.getEmail());
        if (user == null) {
            log.warn("登入失敗：Email {} 尚未註冊", userLoginRequest.getEmail());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }
        String hashedPassword = DigestUtils.md5DigestAsHex(
                userLoginRequest.getPassword().getBytes(StandardCharsets.UTF_8));
        if (!user.getPassword().equals(hashedPassword)) {
            log.warn("登入失敗：Email {} 的密碼錯誤", userLoginRequest.getEmail());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }
        return user;
    }
}
