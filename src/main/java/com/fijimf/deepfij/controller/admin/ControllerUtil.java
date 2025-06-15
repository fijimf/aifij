package com.fijimf.deepfij.controller.admin;

import com.fijimf.deepfij.auth.util.JwtUtil;
import com.fijimf.deepfij.model.User;
import com.fijimf.deepfij.repo.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ControllerUtil {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Autowired
    public ControllerUtil(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    User getUser(HttpServletRequest request) {
        String token = request.getHeader("Authorization").replace("Bearer ", "");
        return userRepository.findByUsername(jwtUtil.extractUsername(token));
    }
}
