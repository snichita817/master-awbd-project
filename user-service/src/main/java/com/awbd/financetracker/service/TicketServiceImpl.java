package com.awbd.financetracker.service;

import com.awbd.financetracker.entity.Ticket;
import com.awbd.financetracker.entity.TicketReply;
import com.awbd.financetracker.entity.User;
import com.awbd.financetracker.enums.TicketStatus;
import com.awbd.financetracker.exception.ResourceNotFoundException;
import com.awbd.financetracker.repository.TicketRepository;
import com.awbd.financetracker.repository.TicketReplyRepository;
import com.awbd.financetracker.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TicketServiceImpl implements TicketService {

    private static final Logger log = LoggerFactory.getLogger(TicketServiceImpl.class);

    private final TicketRepository ticketRepository;
    private final TicketReplyRepository ticketReplyRepository;
    private final UserRepository userRepository;

    public TicketServiceImpl(TicketRepository ticketRepository,
                             TicketReplyRepository ticketReplyRepository,
                             UserRepository userRepository) {
        this.ticketRepository = ticketRepository;
        this.ticketReplyRepository = ticketReplyRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Ticket submitTicket(Long userId, String subject, String message) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        Ticket ticket = new Ticket();
        ticket.setSubject(subject);
        ticket.setMessage(message);
        ticket.setSubmittedBy(user);
        Ticket saved = ticketRepository.save(ticket);
        log.info("Ticket submitted: id={}, subject='{}', userId={}", saved.getId(), subject, userId);
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Ticket> getTicketsForUser(Long userId) {
        return ticketRepository.findBySubmittedByIdOrderByCreatedAtDesc(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Ticket getTicketForUser(Long ticketId, Long userId) {
        Ticket ticket = ticketRepository.findByIdWithReplies(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketId));
        if (!ticket.getSubmittedBy().getId().equals(userId)) {
            throw new AccessDeniedException("Access denied to ticket: " + ticketId);
        }
        return ticket;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Ticket> getAllTickets() {
        return ticketRepository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Ticket> getAllTicketsByStatus(TicketStatus status) {
        return ticketRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    @Override
    @Transactional(readOnly = true)
    public Ticket getTicketById(Long id) {
        return ticketRepository.findByIdWithReplies(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + id));
    }

    @Override
    public TicketReply addReply(Long ticketId, Long authorId, String message, boolean isAdmin) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketId));
        if (!isAdmin && !ticket.getSubmittedBy().getId().equals(authorId)) {
            throw new AccessDeniedException("Access denied to ticket: " + ticketId);
        }
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + authorId));
        TicketReply reply = new TicketReply();
        reply.setMessage(message);
        reply.setTicket(ticket);
        reply.setAuthor(author);
        TicketReply saved = ticketReplyRepository.save(reply);
        log.info("Reply added: ticketId={}, authorId={}", ticketId, authorId);
        return saved;
    }

    @Override
    public Ticket updateStatus(Long ticketId, Long adminId, TicketStatus newStatus, String resolutionNote) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketId));
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + adminId));
        ticket.setStatus(newStatus);
        ticket.setResolvedBy(admin);
        if (resolutionNote != null && !resolutionNote.isBlank()) {
            ticket.setResolutionNote(resolutionNote);
        }
        Ticket saved = ticketRepository.save(ticket);
        log.info("Ticket status updated: id={}, newStatus={}, adminId={}", ticketId, newStatus, adminId);
        return saved;
    }
}
