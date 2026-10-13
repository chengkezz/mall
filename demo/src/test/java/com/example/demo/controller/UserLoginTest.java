package com.example.demo.controller;

import com.example.demo.dao.UserDao;
import com.example.demo.model.User;
import com.example.demo.service.impl.UserServiceImpl;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.slf4j.LoggerFactory;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class UserLoginTest {
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
    void returnsUserWithoutPasswordForCorrectCredentials() throws Exception {
        User user = new User();
        user.setUserId(42);
        user.setEmail("login@example.com");
        user.setPassword("dfb450efddbb5387197c84460623675b");
        when(dao.getUserByEmail("login@example.com")).thenReturn(user);
        mvc.perform(post("/users/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"login@example.com\",\"password\":\"test-password\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(42))
                .andExpect(jsonPath("$.email").value("login@example.com"))
                .andExpect(jsonPath("$.password").doesNotExist());
        verify(dao).getUserByEmail("login@example.com");
        verifyNoMoreInteractions(dao);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void rejectsUnknownEmailOrWrongPasswordAndLogsWarn(boolean exists) throws Exception {
        if (exists) {
            User user = new User();
            user.setPassword("d6275867af19549fcc7106a91db4fe72");
            when(dao.getUserByEmail("login@example.com")).thenReturn(user);
        }
        Logger logger = (Logger) LoggerFactory.getLogger(UserServiceImpl.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        try {
            var result = mvc.perform(post("/users/login").contentType(MediaType.APPLICATION_JSON)
                            .content("{\"email\":\"login@example.com\",\"password\":\"wrong-secret\"}"))
                    .andExpect(status().isUnauthorized()).andReturn();
            assertEquals("Invalid email or password",
                    ((org.springframework.web.server.ResponseStatusException) result.getResolvedException()).getReason());
            String expected = exists ? "登入失敗：Email login@example.com 的密碼錯誤"
                    : "登入失敗：Email login@example.com 尚未註冊";
            assertTrue(appender.list.stream().anyMatch(e -> e.getLevel() == Level.WARN
                    && expected.equals(e.getFormattedMessage())));
            assertTrue(appender.list.stream().noneMatch(e -> e.getFormattedMessage().contains("wrong-secret")
                    || e.getFormattedMessage().contains("stored-secret")));
            verify(dao).getUserByEmail("login@example.com");
            verifyNoMoreInteractions(dao);
        } finally {
            logger.detachAppender(appender);
            appender.stop();
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "null", "{\"password\":\"test\"}",
            "{\"email\":\"login@example.com\"}",
            "{\"email\":\"bad-email\",\"password\":\"test\"}",
            "{\"email\":\"   \",\"password\":\"test\"}",
            "{\"email\":\"login@example.com\",\"password\":\"   \"}"})
    void rejectsInvalidInputBeforeDao(String body) throws Exception {
        mvc.perform(post("/users/login").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(dao);
    }
}
