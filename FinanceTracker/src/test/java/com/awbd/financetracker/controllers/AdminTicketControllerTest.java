package com.awbd.financetracker.controllers;

import com.awbd.financetracker.config.SecurityConfig;
import com.awbd.financetracker.entity.Ticket;
import com.awbd.financetracker.entity.User;
import com.awbd.financetracker.enums.TicketStatus;
import com.awbd.financetracker.service.TicketService;
import com.awbd.financetracker.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminTicketController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class AdminTicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TicketService ticketService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    // --- Access control: regular user must be blocked ---

    @Test
    @WithMockUser(roles = "USER")
    void listTickets_asUser_shouldReturn403() throws Exception {
        mockMvc.perform(get("/admin/tickets"))
                .andExpect(status().isForbidden());
    }

    @Test
    void listTickets_unauthenticated_shouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/admin/tickets"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(roles = "USER")
    void ticketDetail_asUser_shouldReturn403() throws Exception {
        mockMvc.perform(get("/admin/tickets/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateStatus_asUser_shouldReturn403() throws Exception {
        mockMvc.perform(post("/admin/tickets/1/status")
                        .with(csrf())
                        .param("newStatus", "RESOLVED"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void addReply_asUser_shouldReturn403() throws Exception {
        mockMvc.perform(post("/admin/tickets/1/reply")
                        .with(csrf())
                        .param("message", "sneaky reply"))
                .andExpect(status().isForbidden());
    }

    // --- Happy path (admin) ---

    @Test
    @WithMockUser(roles = "ADMIN", username = "admin@financetracker.com")
    void listTickets_asAdmin_shouldReturn200() throws Exception {
        when(ticketService.getAllTickets()).thenReturn(List.of());
        mockMvc.perform(get("/admin/tickets"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN", username = "admin@financetracker.com")
    void ticketDetail_asAdmin_shouldReturn200() throws Exception {
        User submitter = new User();
        submitter.setId(9L);
        submitter.setName("Bob");
        submitter.setEmail("bob@example.com");

        Ticket ticket = new Ticket();
        ticket.setId(1L);
        ticket.setSubject("Help");
        ticket.setMessage("I need help");
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setSubmittedBy(submitter);
        ticket.setCreatedAt(LocalDateTime.now());
        ticket.setUpdatedAt(LocalDateTime.now());

        when(ticketService.getTicketById(1L)).thenReturn(ticket);

        mockMvc.perform(get("/admin/tickets/1"))
                .andExpect(status().isOk());
    }
}

