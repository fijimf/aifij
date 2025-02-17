package com.fijimf.deepfij.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.fijimf.deepfij.model.User;
import com.fijimf.deepfij.repo.RoleRepository;
import com.fijimf.deepfij.repo.UserRepository;

@Service
public class UserService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository; // You'll need to create this
    @Autowired
    private RoleRepository roleRepository;

    public UserDetails createUser(String username, String rawPassword, java.util.List<String> roles) {
        String encodedPassword = passwordEncoder.encode(rawPassword);
        User user = new User();
        user.setUsername(username);
        user.setPassword(encodedPassword);
        user.setEnabled(true);
        // Set any other required user fields

        user = userRepository.save(user);
        User finalUser = user;
        for (String role : roles) {
            finalUser.getRoles().add(roleRepository.findOrCreate(role));
        }
        user = userRepository.save(user);

        return getUserDetails(user);
    }

    public UserDetails updatePassword(User u, String rawPassword) {
        String encodedPassword = passwordEncoder.encode(rawPassword);
        u.setPassword(encodedPassword);
        u.setEnabled(true);
        User user = userRepository.save(u);
        return getUserDetails(user);
    }

    public static UserDetails getUserDetails(User u) {
        return org.springframework.security.core.userdetails.User
                .withUsername(u.getUsername())
                .password(u.getPassword())
                .disabled(!u.isEnabled())
                .build();
    }
}
