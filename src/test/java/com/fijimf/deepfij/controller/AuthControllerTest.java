package com.fijimf.deepfij.controller;

import com.fijimf.deepfij.auth.util.JwtUtil;
import com.fijimf.deepfij.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;


    private final JwtUtil jwtUtil = Mockito.mock(JwtUtil.class);


    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private UserService userService;

    private AuthController authController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authController = new AuthController(authenticationManager, jwtUtil, userDetailsService, userService);
    }

    @Test
    void createAuthenticationToken_ShouldReturnToken() {
        // Arrange
        AuthRequest authRequest = new AuthRequest("testUser", "testPassword");
        UserDetails userDetails = mock(UserDetails.class);
        String expectedToken = "testToken";

        when(userDetailsService.loadUserByUsername(anyString())).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("testUser");
        when(jwtUtil.generateToken(anyString())).thenReturn(expectedToken);

        // Act
        ResponseEntity<Map<String, String>> response = authController.createAuthenticationToken(authRequest);

        // Assert
        verify(authenticationManager).authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
        );
        verify(userDetailsService).loadUserByUsername(authRequest.getUsername());
        verify(jwtUtil).generateToken("testUser");

        assertNotNull(response.getBody());
        assertEquals(expectedToken, response.getBody().get("token"));
    }

    @Test
    void createAuthenticationToken_ShouldFailIfAuthFails() {
        // Arrange
        AuthRequest authRequest = new AuthRequest("testUser", "testPassword");
        UserDetails userDetails = mock(UserDetails.class);
        String expectedToken = "testToken";

        when(userDetailsService.loadUserByUsername(anyString())).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("testUser");
        when(jwtUtil.generateToken(anyString())).thenReturn(expectedToken);
        when(authenticationManager.authenticate(any())).thenThrow(new AuthenticationServiceException("Auth failed"));
        // Act
        ResponseEntity<Map<String, String>> response = authController.createAuthenticationToken(authRequest);

        // Assert

        assertNull(response.getBody());
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void createAuthenticationToken_ShouldFailIfUserNotFound() {
        // Arrange
        AuthRequest authRequest = new AuthRequest("testUser", "testPassword");
        UserDetails userDetails = mock(UserDetails.class);
        String expectedToken = "testToken";

        when(userDetailsService.loadUserByUsername(anyString())).thenThrow(new UsernameNotFoundException("User not found with username:"));
        when(userDetails.getUsername()).thenReturn("testUser");
        when(jwtUtil.generateToken(anyString())).thenReturn(expectedToken);

        // Act
        ResponseEntity<Map<String, String>> response = authController.createAuthenticationToken(authRequest);

        // Assert
        verify(authenticationManager).authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
        );
        verify(userDetailsService).loadUserByUsername(authRequest.getUsername());

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void register_ShouldSucceedWithValidRequest() {
        // Arrange
        AuthRequest registerRequest = new AuthRequest("testUser", "testPassword");

        when(userDetailsService.loadUserByUsername(anyString())).thenThrow(new UsernameNotFoundException("User not found with username:"));

        when(userService.createUser("testUser", "testPassword", List.of("USER"))).thenReturn(org.springframework.security.core.userdetails.User
                .withUsername("testUser")
                .password("testPassword")
                .disabled(false)
                .build());

        when(jwtUtil.generateToken(anyString())).thenReturn("testToken");

        // Act
        ResponseEntity<Map<String, String>> response = authController.register(registerRequest);
        // Assert
        verify(userService).createUser("testUser", "testPassword", List.of("USER"));
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("token"));

    }

    @Test
    void register_ShouldFailWithBlankArgs() {
        // Arrange
        AuthRequest registerRequest = new AuthRequest("", "testPassword");


        // Act
        ResponseEntity<Map<String, String>> response = authController.register(registerRequest);
        // Assert

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue( response.getBody().containsKey("error"));

    }
    @Test
    void register_ShouldFailWithDuplicateUsername() {
        // Arrange
        AuthRequest registerRequest = new AuthRequest("testUser", "testPassword");

        UserDetails userDetails = mock(UserDetails.class);
        when(userDetailsService.loadUserByUsername(anyString())).thenReturn(userDetails);

        // Act
        ResponseEntity<Map<String, String>> response = authController.register(registerRequest);
        // Assert

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue( response.getBody().containsKey("error"));

    }



}