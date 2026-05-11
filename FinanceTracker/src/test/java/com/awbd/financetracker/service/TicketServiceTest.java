package com.awbd.financetracker.service;

import com.awbd.financetracker.entity.Ticket;
import com.awbd.financetracker.entity.TicketReply;
import com.awbd.financetracker.entity.User;
import com.awbd.financetracker.enums.TicketStatus;
import com.awbd.financetracker.exception.ResourceNotFoundException;
import com.awbd.financetracker.repository.TicketRepository;
import com.awbd.financetracker.repository.TicketReplyRepository;
import com.awbd.financetracker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private TicketReplyRepository ticketReplyRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TicketServiceImpl ticketService;

    private User user;
    private User admin;
    private Ticket ticket;

    @BeforeEach
    void setUp() {
        user = new User("Alice", "alice@example.com", new BigDecimal("3000.00"));
        user.setId(1L);

        admin = new User("Admin", "admin@example.com", new BigDecimal("5000.00"));
        admin.setId(2L);

        ticket = new Ticket();
        ticket.setId(10L);
        ticket.setSubject("Help needed");
        ticket.setMessage("I cannot access my account");
        ticket.setSubmittedBy(user);
    }

    // -----------------------------------------------------------------------
    // submitTicket
    // -----------------------------------------------------------------------

    @Test
    void submitTicket_happyPath_savesAndReturnsTicket() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);

        Ticket result = ticketService.submitTicket(1L, "Help needed", "I cannot access my account");

        assertThat(result.getSubject()).isEqualTo("Help needed");
        assertThat(result.getSubmittedBy()).isEqualTo(user);
        verify(ticketRepository).save(any(Ticket.class));
    }

    @Test
    void submitTicket_userNotFound_throwsResourceNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketService.submitTicket(99L, "Subject", "Message"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        verify(ticketRepository, never()).save(any());
    }

    // -----------------------------------------------------------------------
    // getTicketsForUser
    // -----------------------------------------------------------------------

    @Test
    void getTicketsForUser_returnsList() {
        when(ticketRepository.findBySubmittedByIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(ticket));

        List<Ticket> result = ticketService.getTicketsForUser(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSubject()).isEqualTo("Help needed");
    }

    // -----------------------------------------------------------------------
    // getTicketForUser
    // -----------------------------------------------------------------------

    @Test
    void getTicketForUser_happyPath_returnsTicket() {
        when(ticketRepository.findByIdWithReplies(10L)).thenReturn(Optional.of(ticket));

        Ticket result = ticketService.getTicketForUser(10L, 1L);

        assertThat(result.getId()).isEqualTo(10L);
    }

    @Test
    void getTicketForUser_notFound_throwsResourceNotFoundException() {
        when(ticketRepository.findByIdWithReplies(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketService.getTicketForUser(99L, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getTicketForUser_wrongUser_throwsAccessDeniedException() {
        when(ticketRepository.findByIdWithReplies(10L)).thenReturn(Optional.of(ticket));

        assertThatThrownBy(() -> ticketService.getTicketForUser(10L, 999L))
                .isInstanceOf(AccessDeniedException.class);
    }

    // -----------------------------------------------------------------------
    // getAllTickets / getAllTicketsByStatus
    // -----------------------------------------------------------------------

    @Test
    void getAllTickets_returnsList() {
        when(ticketRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(ticket));

        List<Ticket> result = ticketService.getAllTickets();

        assertThat(result).hasSize(1);
    }

    @Test
    void getAllTicketsByStatus_open_returnsList() {
        when(ticketRepository.findByStatusOrderByCreatedAtDesc(TicketStatus.OPEN))
                .thenReturn(List.of(ticket));

        List<Ticket> result = ticketService.getAllTicketsByStatus(TicketStatus.OPEN);

        assertThat(result).hasSize(1);
    }

    // -----------------------------------------------------------------------
    // getTicketById
    // -----------------------------------------------------------------------

    @Test
    void getTicketById_happyPath_returnsTicket() {
        when(ticketRepository.findByIdWithReplies(10L)).thenReturn(Optional.of(ticket));

        Ticket result = ticketService.getTicketById(10L);

        assertThat(result.getId()).isEqualTo(10L);
    }

    @Test
    void getTicketById_notFound_throwsResourceNotFoundException() {
        when(ticketRepository.findByIdWithReplies(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketService.getTicketById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // -----------------------------------------------------------------------
    // addReply
    // -----------------------------------------------------------------------

    @Test
    void addReply_asAdmin_savesReply() {
        TicketReply reply = new TicketReply();
        reply.setMessage("We are looking into it");
        reply.setTicket(ticket);
        reply.setAuthor(admin);

        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(2L)).thenReturn(Optional.of(admin));
        when(ticketReplyRepository.save(any(TicketReply.class))).thenReturn(reply);

        TicketReply result = ticketService.addReply(10L, 2L, "We are looking into it", true);

        assertThat(result.getMessage()).isEqualTo("We are looking into it");
        verify(ticketReplyRepository).save(any(TicketReply.class));
    }

    @Test
    void addReply_asOwnerUser_savesReply() {
        TicketReply reply = new TicketReply();
        reply.setMessage("Any update?");
        reply.setTicket(ticket);
        reply.setAuthor(user);

        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(ticketReplyRepository.save(any(TicketReply.class))).thenReturn(reply);

        TicketReply result = ticketService.addReply(10L, 1L, "Any update?", false);

        assertThat(result.getMessage()).isEqualTo("Any update?");
    }

    @Test
    void addReply_nonOwnerNonAdmin_throwsAccessDeniedException() {
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));

        assertThatThrownBy(() -> ticketService.addReply(10L, 999L, "Sneaky reply", false))
                .isInstanceOf(AccessDeniedException.class);

        verify(ticketReplyRepository, never()).save(any());
    }

    @Test
    void addReply_ticketNotFound_throwsResourceNotFoundException() {
        when(ticketRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketService.addReply(99L, 1L, "msg", false))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // -----------------------------------------------------------------------
    // updateStatus
    // -----------------------------------------------------------------------

    @Test
    void updateStatus_withResolutionNote_updatesTicket() {
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(2L)).thenReturn(Optional.of(admin));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

        Ticket result = ticketService.updateStatus(10L, 2L, TicketStatus.RESOLVED, "Issue fixed");

        assertThat(result.getStatus()).isEqualTo(TicketStatus.RESOLVED);
        assertThat(result.getResolvedBy()).isEqualTo(admin);
        assertThat(result.getResolutionNote()).isEqualTo("Issue fixed");
    }

    @Test
    void updateStatus_blankResolutionNote_noteNotSet() {
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
        when(userRepository.findById(2L)).thenReturn(Optional.of(admin));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

        Ticket result = ticketService.updateStatus(10L, 2L, TicketStatus.IN_PROGRESS, "  ");

        assertThat(result.getStatus()).isEqualTo(TicketStatus.IN_PROGRESS);
        assertThat(result.getResolutionNote()).isNull();
    }

    @Test
    void updateStatus_ticketNotFound_throwsResourceNotFoundException() {
        when(ticketRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketService.updateStatus(99L, 2L, TicketStatus.RESOLVED, "note"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }
}
