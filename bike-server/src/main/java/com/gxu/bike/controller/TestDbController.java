package com.gxu.bike.controller;

import com.gxu.bike.entity.User;
import com.gxu.bike.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/test")
public class TestDbController {

    @Autowired
    private UserMapper userMapper;

    @GetMapping("/users")
    public List<User> getUsers() {
        return userMapper.selectList(null);
    }
}
