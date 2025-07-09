package com.exponent.demo.configuration;

import com.exponent.demo.dto.UserData;
import com.exponent.demo.repo.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MyUserDetailsService implements UserDetailsService {


    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        log.info("user request .....");
        return new UserPrincipal(userRepository.findByUsername(username).map(
                data -> new UserData(data.getFullname(), data.getUsername(), data.getEmail(), data.getPassword())
        ).orElse(new UserData()));
    }
}