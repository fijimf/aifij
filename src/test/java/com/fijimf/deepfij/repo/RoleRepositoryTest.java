package com.fijimf.deepfij.repo;

import com.fijimf.deepfij.DeepFijApplication;
import com.fijimf.deepfij.model.Role;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = DeepFijApplication.class)
public class RoleRepositoryTest {

    @Autowired
    private RoleRepository roleRepository;

    @Container
    private static final SharedPostgreSQLContainer postgreSQLContainer = SharedPostgreSQLContainer.getInstance();

    @BeforeAll
    static void setup() {
        postgreSQLContainer.start();
        System.setProperty("spring.datasource.url", postgreSQLContainer.getJdbcUrl());
        System.setProperty("spring.datasource.username", postgreSQLContainer.getUsername());
        System.setProperty("spring.datasource.password", postgreSQLContainer.getPassword());
    }

    @AfterAll
    static void teardown() {
        postgreSQLContainer.stop();
    }

    @Test
    public void testRepoExists() {
        assertNotNull(roleRepository);
    }

    @Test
    public void testInsertRole() {
        // Happy Path: Insert a valid role
        Role role = new Role();
        role.setName("ROLE_ADMIN");

        Role savedRole = roleRepository.save(role);
        assertNotNull(savedRole.getId(), "Role ID should be generated.");
        assertEquals("ROLE_ADMIN", savedRole.getName());
    }

    @Test
    public void testInsertRole_DuplicateName() {
        // Sad Path: Insert invalid role (duplicate name)
        Role role1 = new Role();
        role1.setName("ROLE_USER");

        Role role2 = new Role();
        role2.setName("ROLE_USER"); // Duplicate name

        roleRepository.save(role1);

        assertThrows(Exception.class, () -> roleRepository.save(role2), "Should throw exception for duplicate role name.");
    }

    @Test
    public void testFindRole() {
        // Happy Path: Find a role by name
        Role role = new Role();
        role.setName("ROLE_ADMIN");
        roleRepository.save(role);

        Role foundRole = roleRepository.findById(role.getId()).orElse(null);

        assertNotNull(foundRole);
        assertEquals("ROLE_ADMIN", foundRole.getName());
    }

    @Test
    public void testFindRole_MissingId() {
        // Sad Path: Find a nonexistent role
        Optional<Role> role = roleRepository.findById(999L);
        assertTrue(role.isEmpty(), "Role with ID 999 should not exist.");
    }

    @Test
    public void testUpdateRole() {
        // Happy Path: Update a valid role
        Role role = new Role();
        role.setName("ROLE_USER");
        role = roleRepository.save(role);

        role.setName("ROLE_MODERATOR");
        Role updatedRole = roleRepository.save(role);

        assertEquals("ROLE_MODERATOR", updatedRole.getName());
    }

    @Test
    public void testDeleteRole() {
        // Happy Path: Delete a role
        Role role = new Role();
        role.setName("ROLE_ADMIN");
        role = roleRepository.save(role);

        roleRepository.delete(role);

        Optional<Role> foundRole = roleRepository.findById(role.getId());
        assertTrue(foundRole.isEmpty(), "Role should be deleted.");
    }

    @Test
    public void testDeleteRole_MissingId() {
        // Sad Path: Delete a nonexistent role
        assertDoesNotThrow(() -> {
            roleRepository.deleteById(999L); // Nonexistent role ID
        }, "Deleting a nonexistent role should throw exception.");
    }
}