package com.fijimf.deepfij.repo;

import com.fijimf.deepfij.DeepFijApplication;
import com.fijimf.deepfij.model.Role;
import com.fijimf.deepfij.model.User;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = DeepFijApplication.class)
public class UserRepositoryTest {

    @Container
    private static final PostgreSQLContainer<?> postgreSQLContainer = SharedPostgreSQLContainer.getInstance();

    @BeforeAll
    static void setup() {
        // Ensure the container is started before setting system properties
        postgreSQLContainer.start(); // Explicitly ensure it starts
        System.setProperty("spring.datasource.url", postgreSQLContainer.getJdbcUrl());
        System.setProperty("spring.datasource.username", postgreSQLContainer.getUsername());
        System.setProperty("spring.datasource.password", postgreSQLContainer.getPassword());
    }

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Test
    public void testInsertUser_HappyPath() {
        // Happy Path: Insert a valid user
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("testpassword");
        user.setEnabled(true);

        User savedUser = userRepository.save(user);
        assertNotNull(savedUser.getId(), "User ID should be generated.");
        assertEquals("testuser", savedUser.getUsername());
    }

    @Test
    public void testAddRoleToUser() {
        // Test adding roles to a user
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("testpassword");
        user.setEnabled(true);
        user = userRepository.save(user);

        Role role = new Role();
        role.setName("ROLE_ADMIN");
        role = roleRepository.save(role);

        user.getRoles().add(role);
        User updatedUser = userRepository.save(user);

        assertTrue(updatedUser.getRoles().contains(role), "User should have the assigned role.");
    }

    @Test
    public void testRetrieveUserRoles() {
        // Test fetching roles associated with a user
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("testpassword");
        user.setEnabled(true);
        user = userRepository.save(user);

        Role role = new Role();
        role.setName("ROLE_USER");
        role = roleRepository.save(role);

        user.getRoles().add(role);
        user = userRepository.save(user);

        User retrievedUser = userRepository.findById(user.getId()).orElse(null);
        assertNotNull(retrievedUser);
        assertTrue(retrievedUser.getRoles().contains(role), "Retrieved roles should include 'ROLE_USER'.");
    }

    @Test
    public void testUpdateUserRoles() {
        // Test updating roles for a user
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("testpassword");
        user.setEnabled(true);
        user = userRepository.save(user);

        Role role1 = new Role();
        role1.setName("ROLE_USER");
        role1 = roleRepository.save(role1);

        Role role2 = new Role();
        role2.setName("ROLE_MODERATOR");
        role2 = roleRepository.save(role2);

        user.getRoles().add(role1);
        user = userRepository.save(user);

        // Add another role
        user.getRoles().add(role2);
        user = userRepository.save(user);

        assertTrue(user.getRoles().containsAll(java.util.List.of(role1, role2)), "User should have updated roles.");
    }

    @Test
    public void testRemoveUserRole() {
        // Test removing a role from a user
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("testpassword");
        user.setEnabled(true);
        user = userRepository.save(user);

        Role role = new Role();
        role.setName("ROLE_ADMIN");
        role = roleRepository.save(role);

        user.getRoles().add(role);
        user = userRepository.save(user);

        // Remove the role
        user.getRoles().remove(role);
        User updatedUser = userRepository.save(user);

        assertFalse(updatedUser.getRoles().contains(role), "User should not have the removed role.");
    }

    @Test
    public void testInsertUser_SadPath() {
        // Sad Path: Insert invalid user (username already exists)
        User user1 = new User();
        user1.setUsername("duplicateuser");
        user1.setPassword("password");
        user1.setEnabled(true);

        User user2 = new User();
        user2.setUsername("duplicateuser"); // Duplicate username
        user2.setPassword("password");
        user2.setEnabled(true);

        userRepository.save(user1);

        assertThrows(Exception.class, () -> {
            userRepository.save(user2);
        }, "Should throw exception for duplicate username.");
    }

    @Test
    public void testFindUser_HappyPath() {
        // Happy Path: Find a user
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("testpassword");
        userRepository.save(user);

        User foundUser = userRepository.findById(user.getId()).orElse(null);

        assertNotNull(foundUser);
        assertEquals("testuser", foundUser.getUsername());
    }

    @Test
    public void testDeleteUser_HappyPath() {
        // Happy Path: Delete a user
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("testpassword");
        user = userRepository.save(user);

        userRepository.delete(user);

        Optional<User> foundUser = userRepository.findById(user.getId());
        assertTrue(foundUser.isEmpty(), "User should be deleted.");
    }

    @Test
    public void testUpdateUser_HappyPath() {
        // Happy Path: Update a valid user
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("testpassword");
        user = userRepository.save(user);

        user.setPassword("newpassword");
        User updatedUser = userRepository.save(user);

        assertEquals("newpassword", updatedUser.getPassword());
    }

}