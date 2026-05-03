package com.awbd.financetracker.controllers;

import com.awbd.financetracker.exception.DuplicateResourceException;
import com.awbd.financetracker.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerForm", new RegisterForm("", "", "", null));
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registerForm") RegisterForm form,
                           BindingResult result,
                           RedirectAttributes redirectAttrs) {
        if (result.hasErrors()) {
            return "register";
        }
        try {
            userService.registerUser(form.name(), form.email(), form.password(), form.monthlyIncome());
        } catch (DuplicateResourceException ex) {
            result.rejectValue("email", "duplicate", ex.getMessage());
            return "register";
        }
        redirectAttrs.addFlashAttribute("successMessage", "Account created! Please sign in.");
        return "redirect:/login";
    }

    public record RegisterForm(
            @NotBlank(message = "Name is required") String name,
            @NotNull(message = "Email is required") @Email(message = "Email should be valid") String email,
            @NotBlank(message = "Password is required") @Size(min = 8, message = "Password must be at least 8 characters") String password,
            @NotNull(message = "Monthly income is required") @DecimalMin(value = "0.0", message = "Monthly income must be positive") BigDecimal monthlyIncome
    ) {}
}
