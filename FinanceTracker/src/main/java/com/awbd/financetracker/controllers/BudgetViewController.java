package com.awbd.financetracker.controllers;

import com.awbd.financetracker.entity.Budget;
import com.awbd.financetracker.exception.DuplicateResourceException;
import com.awbd.financetracker.exception.ResourceNotFoundException;
import com.awbd.financetracker.service.BudgetService;
import com.awbd.financetracker.service.CategoryService;
import com.awbd.financetracker.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/budgets")
public class BudgetViewController {

    private final BudgetService budgetService;
    private final CategoryService categoryService;
    private final UserService userService;

    public BudgetViewController(BudgetService budgetService,
                                CategoryService categoryService,
                                UserService userService) {
        this.budgetService = budgetService;
        this.categoryService = categoryService;
        this.userService = userService;
    }

    @GetMapping
    public String list(@AuthenticationPrincipal UserDetails principal, Model model) {
        userService.getUserByEmail(principal.getUsername()).ifPresent(user -> {
            model.addAttribute("budgets", budgetService.getBudgetsByUserId(user.getId()));
        });
        return "budgets/list";
    }

    @GetMapping("/new")
    public String newForm(@AuthenticationPrincipal UserDetails principal, Model model) {
        userService.getUserByEmail(principal.getUsername()).ifPresent(user -> {
            model.addAttribute("categories", categoryService.getCategoriesByUserId(user.getId()));
        });
        model.addAttribute("budget", new Budget());
        model.addAttribute("formAction", "/budgets");
        return "budgets/form";
    }

    @PostMapping
    public String create(@AuthenticationPrincipal UserDetails principal,
                         @RequestParam Long categoryId,
                         @Valid @ModelAttribute("budget") Budget budget,
                         BindingResult result,
                         RedirectAttributes redirectAttrs,
                         Model model) {
        if (result.hasErrors()) {
            userService.getUserByEmail(principal.getUsername()).ifPresent(user ->
                model.addAttribute("categories", categoryService.getCategoriesByUserId(user.getId())));
            model.addAttribute("selectedCategoryId", categoryId);
            model.addAttribute("formAction", "/budgets");
            return "budgets/form";
        }
        try {
            budgetService.createBudget(categoryId, budget);
        } catch (DuplicateResourceException | ResourceNotFoundException ex) {
            result.reject("error", ex.getMessage());
            userService.getUserByEmail(principal.getUsername()).ifPresent(user ->
                model.addAttribute("categories", categoryService.getCategoriesByUserId(user.getId())));
            model.addAttribute("selectedCategoryId", categoryId);
            model.addAttribute("formAction", "/budgets");
            return "budgets/form";
        }
        redirectAttrs.addFlashAttribute("successMessage", "Budget created successfully.");
        return "redirect:/budgets";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Budget budget = budgetService.getBudgetById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found: " + id));
        model.addAttribute("budget", budget);
        model.addAttribute("formAction", "/budgets/" + id);
        return "budgets/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("budget") Budget budget,
                         BindingResult result,
                         RedirectAttributes redirectAttrs,
                         Model model) {
        if (result.hasErrors()) {
            model.addAttribute("formAction", "/budgets/" + id);
            return "budgets/form";
        }
        try {
            budgetService.updateBudget(id, budget);
        } catch (ResourceNotFoundException ex) {
            result.reject("error", ex.getMessage());
            model.addAttribute("formAction", "/budgets/" + id);
            return "budgets/form";
        }
        redirectAttrs.addFlashAttribute("successMessage", "Budget updated successfully.");
        return "redirect:/budgets";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        budgetService.deleteBudget(id);
        redirectAttrs.addFlashAttribute("successMessage", "Budget deleted.");
        return "redirect:/budgets";
    }
}
