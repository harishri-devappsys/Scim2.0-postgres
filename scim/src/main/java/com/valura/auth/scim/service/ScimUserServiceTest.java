package com.valura.auth.scim.service;

import com.unboundid.scim2.common.types.Email;
import com.unboundid.scim2.common.types.UserResource;
import com.valura.auth.database.repository.UserRepository;
import com.valura.auth.scim.model.ScimListResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ScimUserServiceTest {

    @Autowired
    private ScimUserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void createUser() throws Exception {
        UserResource user = new UserResource();
        user.setUserName("testuser");
        user.setDisplayName("Test User");
        user.setActive(true);

        Email email = new Email();
        email.setValue("test@example.com");
        email.setPrimary(true);
        List<Email> emails = new ArrayList<>();
        emails.add(email);
        user.setEmails(emails);
        UserResource created = userService.create(user);
        assertNotNull(created.getId());
        assertEquals("testuser", created.getUserName());
        assertEquals("Test User", created.getDisplayName());
        assertTrue(created.getActive());
        assertFalse(created.getEmails().isEmpty());
        assertEquals("test@example.com", created.getEmails().get(0).getValue());
    }

    @Test
    void searchUsers() throws Exception {
        // Create test users
        createTestUser("user1", "user1@example.com");
        createTestUser("user2", "user2@example.com");

        ScimListResponse<UserResource> response = userService.search(1, 10, null);

        assertNotNull(response);
        assertEquals(2, response.getTotalResults());
        assertEquals(1, response.getStartIndex());
        assertEquals(10, response.getItemsPerPage());
        assertEquals(2, response.getResources().size());
    }

    private UserResource createTestUser(String username, String email) throws Exception {
        UserResource user = new UserResource();
        user.setUserName(username);
        user.setDisplayName(username);
        user.setActive(true);

        Email emailObj = new Email();
        emailObj.setValue(email);
        emailObj.setPrimary(true);
        List<Email> emails = new ArrayList<>();
        emails.add(emailObj);
        user.setEmails(emails);

        return userService.create(user);
    }
}