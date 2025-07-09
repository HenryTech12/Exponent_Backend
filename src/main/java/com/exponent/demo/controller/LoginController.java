package com.exponent.demo.controller;

import com.exponent.demo.dto.UserData;
import com.exponent.demo.request.AuthRequest;
import com.exponent.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
@RequestMapping("/api/user")
public class LoginController {

    @Autowired
    private UserService userService;

    @PostMapping("/create")
    public ResponseEntity<String> createUser(@RequestBody UserData userData) {
        if(!Objects.isNull(userData))
            userService.createUser(userData);
        return ResponseEntity.ok()
                .body("User Saved To DB");
    }

    @PostMapping("/login")
    public String login(@RequestBody AuthRequest authRequest) {

        return "logged in";
    }
}
