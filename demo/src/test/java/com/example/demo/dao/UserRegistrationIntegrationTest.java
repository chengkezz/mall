package com.example.demo.dao;

import com.example.demo.dto.UserRegisterRequest;
import com.example.demo.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@EnabledIfEnvironmentVariable(named = "MALL_DB_TEST", matches = "true")
class UserRegistrationIntegrationTest {
    @Autowired
    private UserService service;
    @Autowired
    private UserDao dao;

    @Test
    void createsAndReadsUserWithGeneratedIdAndTimestamps() {
        UserRegisterRequest request = new UserRegisterRequest();
        request.setEmail("test-" + UUID.randomUUID() + "@example.com");
        request.setPassword("test-only-password");
        assertNull(dao.getUserByEmail(request.getEmail()));
        Integer id = service.register(request);
        var user = service.getUserById(id);
        assertNotNull(id);
        assertTrue(id > 0);
        assertNotNull(user);
        assertEquals(id, user.getUserId());
        assertEquals(request.getEmail(), user.getEmail());
        assertEquals(request.getPassword(), user.getPassword());
        assertNotNull(user.getCreatedDate());
        assertEquals(user.getCreatedDate(), user.getLastModifiedDate());
        assertNull(dao.getUserById(-1));
        assertEquals(id, dao.getUserByEmail(request.getEmail()).getUserId());
        var exception = assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> service.register(request));
        assertEquals(org.springframework.http.HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals(id, dao.getUserByEmail(request.getEmail()).getUserId());
    }
}
