package com.awbd.financetracker.service;

import com.awbd.financetracker.entity.Ticket;
import com.awbd.financetracker.entity.TicketReply;
import com.awbd.financetracker.enums.TicketStatus;

import java.util.List;

public interface TicketService {

    Ticket submitTicket(Long userId, String subject, String message);

    List<Ticket> getTicketsForUser(Long userId);

    Ticket getTicketForUser(Long ticketId, Long userId);

    List<Ticket> getAllTickets();

    List<Ticket> getAllTicketsByStatus(TicketStatus status);

    Ticket getTicketById(Long id);

    TicketReply addReply(Long ticketId, Long authorId, String message, boolean isAdmin);

    Ticket updateStatus(Long ticketId, Long adminId, TicketStatus newStatus, String resolutionNote);
}
