package com.awbd.financetracker.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.awbd.financetracker.config.SecurityConfig;
import com.awbd.financetracker.entity.User;
import com.awbd.financetracker.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("John Doe");
        testUser.setEmail("john.doe@email.com");
        testUser.setMonthlyIncome(new BigDecimal("5000.00"));
    }

    // -----------------------------------------------------------------------
    // Happy-path & validation tests (run as ADMIN)
    // -----------------------------------------------------------------------

    @Test
    @WithMockUser(roles = "USER")
    void createUser() throws Exception {
        when(userService.createUser("John Doe", "john.doe@email.com", new BigDecimal("5000.00")))
                .thenReturn(testUser);

        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUser)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@email.com"));

        Mockito.verify(userService).createUser("John Doe", "john.doe@email.com", new BigDecimal("5000.00"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserById_WhenNotExists_ShouldReturn404() throws Exception {
        when(userService.getUserById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/99")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        Mockito.verify(userService).getUserById(99L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUser_WithInvalidEmail_ShouldReturnBadRequest() throws Exception {
        testUser.setEmail("invalid-email");

        mockMvc.perform(put("/api/users/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUser)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).updateUser(anyLong(), anyString(), anyString(), any(BigDecimal.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void createUser_WithNegativeIncome_ShouldReturnBadRequest() throws Exception {
        testUser.setMonthlyIncome(new BigDecimal("-1000.00"));

        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUser)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(anyString(), anyString(), any(BigDecimal.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_ShouldReturnNoContent() throws Exception {
        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/users/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_asAdmin_shouldReturn200() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of(testUser));

        mockMvc.perform(get("/api/users").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    @WithMockUser(roles = "USER", username = "john.doe@email.com")
    void getUserById_asOwner_shouldReturn200() throws Exception {
        when(userService.getUserByEmail("john.doe@email.com")).thenReturn(Optional.of(testUser));
        when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));

        mockMvc.perform(get("/api/users/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    // -----------------------------------------------------------------------
    // Access-control tests
    // -----------------------------------------------------------------------

    @Test
    @WithMockUser(roles = "USER")
    void getAllUsers_asUser_shouldReturn403() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER", username = "other@example.com")
    void getUserById_asOtherUser_shouldReturn403() throws Exception {
        when(userService.getUserByEmail("other@example.com")).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER", username = "other@example.com")
    void updateUser_asOtherUser_shouldReturn403() throws Exception {
        when(userService.getUserByEmail("other@example.com")).thenReturn(Optional.empty());
        mockMvc.perform(put("/api/users/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUser)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER", username = "other@example.com")
    void deleteUser_asOtherUser_shouldReturn403() throws Exception {
        when(userService.getUserByEmail("other@example.com")).thenReturn(Optional.empty());
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/users/1").with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void getUser_unauthenticated_shouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().is3xxRedirection());
    }
}
