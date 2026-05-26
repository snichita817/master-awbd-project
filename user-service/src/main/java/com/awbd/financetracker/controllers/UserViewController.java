package com.awbd.financetracker.controllers;

import com.awbd.financetracker.entity.User;
import com.awbd.financetracker.exception.DuplicateResourceException;
import com.awbd.financetracker.exception.ResourceNotFoundException;
import com.awbd.financetracker.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/users")
public class UserViewController {

    private final UserService userService;

    public UserViewController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String list(@RequestParam(defaultValue = "") String search, Model model) {
        model.addAttribute("users", userService.searchUsers(search));
        model.addAttribute("search", search);
        return "users/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("formAction", "/users");
        return "users/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("user") User user,
                         BindingResult result,
                         RedirectAttributes redirectAttrs,
                         Model model) {
        if (result.hasErrors()) {
            model.addAttribute("formAction", "/users");
            return "users/form";
        }
        try {
            userService.createUser(user.getName(), user.getEmail(), user.getMonthlyIncome());
        } catch (DuplicateResourceException ex) {
            result.rejectValue("email", "duplicate", ex.getMessage());
            model.addAttribute("formAction", "/users");
            return "users/form";
        }
        redirectAttrs.addFlashAttribute("successMessage", "User created successfully.");
        return "redirect:/users";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        User user = userService.getUserById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
        model.addAttribute("user", user);
        model.addAttribute("formAction", "/users/" + id);
        return "users/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("user") User user,
                         BindingResult result,
                         RedirectAttributes redirectAttrs,
                         Model model) {
        if (result.hasErrors()) {
            model.addAttribute("formAction", "/users/" + id);
            return "users/form";
        }
        try {
            userService.updateUser(id, user.getName(), user.getEmail(), user.getMonthlyIncome());
        } catch (DuplicateResourceException ex) {
            result.rejectValue("email", "duplicate", ex.getMessage());
            model.addAttribute("formAction", "/users/" + id);
            return "users/form";
        }
        redirectAttrs.addFlashAttribute("successMessage", "User updated successfully.");
        return "redirect:/users";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        userService.deleteUser(id);
        redirectAttrs.addFlashAttribute("successMessage", "User deleted.");
        return "redirect:/users";
    }
}
