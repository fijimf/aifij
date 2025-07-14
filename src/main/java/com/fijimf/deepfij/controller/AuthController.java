package com.fijimf.deepfij.controller;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fijimf.deepfij.auth.util.JwtUtil;
import com.fijimf.deepfij.response.ApiResponse;
import com.fijimf.deepfij.service.UserService;
import com.fijimf.deepfij.validation.PasswordValidator;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
@Validated
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final UserService userService;
    private final PasswordValidator passwordValidator;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil, 
                         UserDetailsService userDetailsService, UserService userService,
                         PasswordValidator passwordValidator) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.userService = userService;
        this.passwordValidator = passwordValidator;
    }

    @PostMapping("/authenticate")
    public ResponseEntity<ApiResponse<Map<String, String>>> createAuthenticationToken(@Valid @RequestBody AuthRequest authRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.username(), authRequest.password())
            );
        } catch (AuthenticationException authenticationException) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Invalid username or password"));
        }
        try {
            UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.username());
            String token = jwtUtil.generateToken(userDetails.getUsername());
            log.info("Authentication successful for user {}", authRequest.username());
            return ResponseEntity.ok(ApiResponse.success("Authentication successful", Map.of("token", token)));
        } catch (UsernameNotFoundException usernameNotFoundException) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Invalid username or password"));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Map<String, String>>> register(@Valid @RequestBody AuthRequest authRequest) {
        try {
            // Validate password complexity
            PasswordValidator.ValidationResult passwordValidation = passwordValidator.validate(authRequest.password());
            if (!passwordValidation.isValid()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Password validation failed: " + String.join(", ", passwordValidation.errors())));
            }
    
            // Check if username already exists
            try {
                userDetailsService.loadUserByUsername(authRequest.username());
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error("Username already exists"));
            } catch (UsernameNotFoundException e) {
                // Username is available, continue with registration
            }
    
            // Create new user
            UserDetails newUser = userService.createUser(authRequest.username(), authRequest.password(), List.of("USER"));
    
            // Generate token for automatic login
            String token = jwtUtil.generateToken(newUser.getUsername());
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", Map.of("token", token)));
                
        } catch (Exception e) {
            log.error("Registration failed for user {}", authRequest.username(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Registration failed. Please try again."));
        }
    }
}