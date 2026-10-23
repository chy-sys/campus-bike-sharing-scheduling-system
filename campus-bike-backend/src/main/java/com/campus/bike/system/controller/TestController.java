package com.campus.bike.system.controller;

import com.campus.bike.modules.user.entity.User;
import com.campus.bike.modules.user.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private UserMapper userMapper;

    @GetMapping("/users")
    public List<User> getUsers() {
        return userMapper.selectList(null);
    }
}
