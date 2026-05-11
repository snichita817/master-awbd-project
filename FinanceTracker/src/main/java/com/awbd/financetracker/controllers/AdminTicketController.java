package com.awbd.financetracker.controllers;

import com.awbd.financetracker.entity.Ticket;
import com.awbd.financetracker.enums.TicketStatus;
import com.awbd.financetracker.service.TicketService;
import com.awbd.financetracker.service.UserService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/tickets")
@PreAuthorize("hasRole('ADMIN')")
public class AdminTicketController {

    private final TicketService ticketService;
    private final UserService userService;

    public AdminTicketController(TicketService ticketService, UserService userService) {
        this.ticketService = ticketService;
        this.userService = userService;
    }

    @GetMapping
    public String listTickets(@RequestParam(required = false) String status, Model model) {
        List<Ticket> tickets;
        if (status != null && !status.isBlank()) {
            try {
                TicketStatus ticketStatus = TicketStatus.valueOf(status.toUpperCase());
                tickets = ticketService.getAllTicketsByStatus(ticketStatus);
                model.addAttribute("activeStatus", ticketStatus);
            } catch (IllegalArgumentException e) {
                tickets = ticketService.getAllTickets();
            }
        } else {
            tickets = ticketService.getAllTickets();
        }
        model.addAttribute("tickets", tickets);
        model.addAttribute("statuses", TicketStatus.values());
        return "admin/tickets/list";
    }

    @GetMapping("/{id}")
    public String ticketDetail(@PathVariable Long id, Model model) {
        Ticket ticket = ticketService.getTicketById(id);
        model.addAttribute("ticket", ticket);
        model.addAttribute("statuses", TicketStatus.values());
        return "admin/tickets/detail";
    }

    @PostMapping("/{id}/reply")
    public String addReply(@PathVariable Long id,
                           @RequestParam @NotBlank String message,
                           @AuthenticationPrincipal UserDetails principal,
                           RedirectAttributes redirectAttributes) {
        userService.getUserByEmail(principal.getUsername()).ifPresent(user ->
                ticketService.addReply(id, user.getId(), message, true)
        );
        redirectAttributes.addFlashAttribute("successMessage", "Reply sent.");
        return "redirect:/admin/tickets/" + id;
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id,
                               @RequestParam String newStatus,
                               @RequestParam(required = false) String resolutionNote,
                               @AuthenticationPrincipal UserDetails principal,
                               RedirectAttributes redirectAttributes) {
        TicketStatus ticketStatus = TicketStatus.valueOf(newStatus.toUpperCase());
        userService.getUserByEmail(principal.getUsername()).ifPresent(user ->
                ticketService.updateStatus(id, user.getId(), ticketStatus, resolutionNote)
        );
        redirectAttributes.addFlashAttribute("successMessage", "Ticket status updated.");
        return "redirect:/admin/tickets/" + id;
    }
}
