package com.example.demo.controller;

import com.example.demo.dao.UserDao;
import com.example.demo.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class UserRegisterTest {
    @Autowired
    private WebApplicationContext context;
    @MockitoBean
    private UserDao dao;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void registersAndReadsUserWithoutReturningPassword() throws Exception {
        User user = new User();
        user.setUserId(42);
        user.setEmail("test@example.com");
        user.setPassword("dfb450efddbb5387197c84460623675b");
        when(dao.createUser(any())).thenReturn(42);
        when(dao.getUserById(42)).thenReturn(user);
        mvc.perform(post("/users/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@example.com\",\"password\":\"test-password\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(42))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.password").doesNotExist());
        var order = inOrder(dao);
        order.verify(dao).getUserByEmail("test@example.com");
        order.verify(dao).createUser(argThat(r -> "test@example.com".equals(r.getEmail())
                && "dfb450efddbb5387197c84460623675b".equals(r.getPassword())));
        order.verify(dao).getUserById(42);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "{}", "{\"password\":\"test\"}", "{\"email\":\"test@example.com\"}",
        "{\"email\":null,\"password\":\"test\"}",
        "{\"email\":\"\",\"password\":\"test\"}",
        "{\"email\":\"   \",\"password\":\"test\"}",
        "{\"email\":\"test@example.com\",\"password\":null}",
        "{\"email\":\"test@example.com\",\"password\":\"\"}",
        "{\"email\":\"test@example.com\",\"password\":\"   \"}"
    })
    void rejectsBlankFieldsBeforeDao(String body) throws Exception {
        mvc.perform(post("/users/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(dao);
    }

    @Test
    void rejectsDuplicateEmailAndLogsWarn() throws Exception {
        when(dao.getUserByEmail("taken@example.com")).thenReturn(new User());
        var logger = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(
                com.example.demo.service.impl.UserServiceImpl.class);
        var appender = new ch.qos.logback.core.read.ListAppender<ch.qos.logback.classic.spi.ILoggingEvent>();
        appender.start();
        logger.addAppender(appender);
        try {
            mvc.perform(post("/users/register").contentType(MediaType.APPLICATION_JSON)
                            .content("{\"email\":\"taken@example.com\",\"password\":\"test-password\"}"))
                    .andExpect(status().isBadRequest());
            verify(dao).getUserByEmail("taken@example.com");
            verify(dao, never()).createUser(any());
            verify(dao, never()).getUserById(any());
            org.junit.jupiter.api.Assertions.assertTrue(appender.list.stream().anyMatch(event ->
                    event.getLevel() == ch.qos.logback.classic.Level.WARN
                    && event.getFormattedMessage().equals("註冊失敗：Email taken@example.com 已被註冊")));
        } finally {
            logger.detachAppender(appender);
            appender.stop();
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"not-an-email", "@example.com", "test@", "test user@example.com"})
    void rejectsInvalidEmailFormat(String email) throws Exception {
        mvc.perform(post("/users/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"test-password\"}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(dao);
    }
}
