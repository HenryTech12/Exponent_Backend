package com.exponent.demo.service;

import com.exponent.demo.dto.UserData;
import com.exponent.demo.model.UserModel;
import com.exponent.demo.repo.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Slf4j
@Service
public class UserService {


    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public void createUser(UserData userData) {
        if(!Objects.isNull(userData)) {
            UserModel userModel = UserModel.builder()
                    .email(userData.getEmail())
                    .fullname(userData.getFullname())
                    .password(passwordEncoder.encode(userData.getPassword()))
                    .username(userData.getUsername())
                    .build();
            userRepository.save(userModel);
            log.info("user details saved to db.");
        }
    }

    public UserData findUserWithUsername(String username) {
        UserModel userModel = userRepository.findByUsername(username).orElse(new UserModel());
        return UserData.builder()
                .email(userModel.getEmail())
                .fullname(userModel.getFullname())
                .password(userModel.getPassword())
                .build();
    }


}
