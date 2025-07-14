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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fijimf.deepfij.auth.util.JwtUtil;
import com.fijimf.deepfij.dto.ForgotPasswordRequest;
import com.fijimf.deepfij.dto.PasswordChangeRequest;
import com.fijimf.deepfij.dto.ResetPasswordRequest;
import com.fijimf.deepfij.dto.TokenRefreshRequest;
import com.fijimf.deepfij.dto.UserProfileResponse;
import com.fijimf.deepfij.model.Role;
import com.fijimf.deepfij.model.User;
import com.fijimf.deepfij.response.ApiResponse;
import com.fijimf.deepfij.service.PasswordResetService;
import com.fijimf.deepfij.service.UserService;
import com.fijimf.deepfij.validation.PasswordValidator;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.stream.Collectors;

@RestController
@Validated
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final UserService userService;
    private final PasswordValidator passwordValidator;
    private final PasswordResetService passwordResetService;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil, 
                         UserDetailsService userDetailsService, UserService userService,
                         PasswordValidator passwordValidator, PasswordResetService passwordResetService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.userService = userService;
        this.passwordValidator = passwordValidator;
        this.passwordResetService = passwordResetService;
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

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<Map<String, String>>> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        try {
            String username = jwtUtil.extractUsername(request.token());
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            
            if (jwtUtil.validateToken(request.token(), username)) {
                String newToken = jwtUtil.generateToken(username);
                return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", Map.of("token", newToken)));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Invalid or expired refresh token"));
            }
        } catch (Exception e) {
            log.error("Token refresh failed", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Token refresh failed"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                jwtUtil.invalidateToken(token);
                return ResponseEntity.ok(ApiResponse.success("Logged out successfully"));
            }
            return ResponseEntity.ok(ApiResponse.success("Logged out successfully"));
        } catch (Exception e) {
            log.error("Logout failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Logout failed"));
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<String>> changePassword(@Valid @RequestBody PasswordChangeRequest request, Principal principal) {
        try {
            if (principal == null) {
                log.warn("Principal is null in changePassword - authentication may not be working correctly");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required"));
            }
            
            String username = principal.getName();
            
            // Verify current password
            try {
                authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, request.currentPassword())
                );
            } catch (AuthenticationException e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Current password is incorrect"));
            }
            
            // Validate new password
            PasswordValidator.ValidationResult validation = passwordValidator.validate(request.newPassword());
            if (!validation.isValid()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("New password validation failed: " + String.join(", ", validation.errors())));
            }
            
            // Update password
            User user = userService.findByUsername(username);
            if (user != null) {
                userService.updatePassword(user, request.newPassword());
                return ResponseEntity.ok(ApiResponse.success("Password changed successfully"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("User not found"));
            }
            
        } catch (Exception e) {
            log.error("Password change failed for user {}", principal != null ? principal.getName() : "unknown", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Password change failed"));
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(Principal principal) {
        try {
            if (principal == null) {
                log.warn("Principal is null in getProfile - authentication may not be working correctly");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required"));
            }
            
            String username = principal.getName();
            User user = userService.findByUsername(username);
            
            if (user != null) {
                UserProfileResponse profile = new UserProfileResponse(
                    user.getId(),
                    user.getUsername(),
                    user.isEnabled(),
                    user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toSet())
                );
                return ResponseEntity.ok(ApiResponse.success(profile));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("User not found"));
            }
        } catch (Exception e) {
            log.error("Failed to get profile for user {}", principal != null ? principal.getName() : "unknown", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to get user profile"));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            User user = userService.findByUsername(request.username());
            if (user != null) {
                String resetToken = passwordResetService.generateResetToken(request.username());
                // In a real application, you would send this token via email
                // For now, we'll just log it (in production, remove this log)
                log.info("Password reset token for user {}: {}", request.username(), resetToken);
                return ResponseEntity.ok(ApiResponse.success("If a user with that username exists, a password reset link has been sent"));
            } else {
                // Don't reveal if user exists or not for security
                return ResponseEntity.ok(ApiResponse.success("If a user with that username exists, a password reset link has been sent"));
            }
        } catch (Exception e) {
            log.error("Forgot password failed for username {}", request.username(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Forgot password request failed"));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            String username = passwordResetService.validateResetToken(request.resetToken());
            if (username == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Invalid or expired reset token"));
            }

            // Validate new password
            PasswordValidator.ValidationResult validation = passwordValidator.validate(request.newPassword());
            if (!validation.isValid()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Password validation failed: " + String.join(", ", validation.errors())));
            }

            // Update password
            User user = userService.findByUsername(username);
            if (user != null) {
                userService.updatePassword(user, request.newPassword());
                passwordResetService.consumeResetToken(request.resetToken());
                return ResponseEntity.ok(ApiResponse.success("Password reset successfully"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("User not found"));
            }
            
        } catch (Exception e) {
            log.error("Password reset failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Password reset failed"));
        }
    }
}