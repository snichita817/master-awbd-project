package com.awbd.financetracker.controllers;

import com.awbd.financetracker.entity.Ticket;
import com.awbd.financetracker.service.TicketService;
import com.awbd.financetracker.service.UserService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/contact")
public class ContactController {

    private final TicketService ticketService;
    private final UserService userService;

    public ContactController(TicketService ticketService, UserService userService) {
        this.ticketService = ticketService;
        this.userService = userService;
    }

    @GetMapping
    public String showForm() {
        return "contact/form";
    }

    @PostMapping
    public String submitTicket(@RequestParam @NotBlank @Size(max = 200) String subject,
                               @RequestParam @NotBlank String message,
                               @AuthenticationPrincipal UserDetails principal,
                               RedirectAttributes redirectAttributes) {
        userService.getUserByEmail(principal.getUsername()).ifPresent(user ->
                ticketService.submitTicket(user.getId(), subject, message)
        );
        redirectAttributes.addFlashAttribute("successMessage", "Your support ticket has been submitted.");
        return "redirect:/contact/my-tickets";
    }

    @GetMapping("/my-tickets")
    public String myTickets(@AuthenticationPrincipal UserDetails principal, Model model) {
        userService.getUserByEmail(principal.getUsername()).ifPresent(user -> {
            List<Ticket> tickets = ticketService.getTicketsForUser(user.getId());
            model.addAttribute("tickets", tickets);
        });
        return "contact/my-tickets";
    }

    @GetMapping("/my-tickets/{id}")
    public String ticketDetail(@PathVariable Long id,
                               @AuthenticationPrincipal UserDetails principal,
                               Model model) {
        userService.getUserByEmail(principal.getUsername()).ifPresent(user -> {
            Ticket ticket = ticketService.getTicketForUser(id, user.getId());
            model.addAttribute("ticket", ticket);
        });
        return "contact/ticket-detail";
    }

    @PostMapping("/my-tickets/{id}/reply")
    public String addReply(@PathVariable Long id,
                           @RequestParam @NotBlank String message,
                           @AuthenticationPrincipal UserDetails principal,
                           RedirectAttributes redirectAttributes) {
        userService.getUserByEmail(principal.getUsername()).ifPresent(user ->
                ticketService.addReply(id, user.getId(), message, false)
        );
        redirectAttributes.addFlashAttribute("successMessage", "Reply added.");
        return "redirect:/contact/my-tickets/" + id;
    }
}
