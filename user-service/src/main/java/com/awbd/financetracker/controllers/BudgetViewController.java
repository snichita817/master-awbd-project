package com.awbd.financetracker.controllers;

import com.awbd.financetracker.client.FinanceCoreClient;
import com.awbd.financetracker.dto.BudgetForm;
import com.awbd.financetracker.entity.User;
import com.awbd.financetracker.exception.ResourceNotFoundException;
import com.awbd.financetracker.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/budgets")
public class BudgetViewController {

    private final FinanceCoreClient financeCoreClient;
    private final UserService userService;

    public BudgetViewController(FinanceCoreClient financeCoreClient, UserService userService) {
        this.financeCoreClient = financeCoreClient;
        this.userService = userService;
    }

    @GetMapping
    public String list(@AuthenticationPrincipal UserDetails principal, Model model) {
        var budgets = financeCoreClient.getBudgets(currentUser(principal).getId());
        Map<String, BigDecimal> spendingByCategory = budgets.stream()
                .filter(budget -> budget.category() != null)
                .collect(Collectors.toMap(
                        budget -> budget.category().name(),
                        budget -> budget.currentSpending() == null ? BigDecimal.ZERO : budget.currentSpending()
                ));
        model.addAttribute("budgets", budgets);
        model.addAttribute("spendingByCategory", spendingByCategory);
        return "budgets/list";
    }

    @GetMapping("/new")
    public String newForm(@AuthenticationPrincipal UserDetails principal,
                          @RequestParam(required = false) Long categoryId,
                          Model model) {
        BudgetForm budget = new BudgetForm();
        budget.setCategoryId(categoryId);
        addFormAttributes(model, budget, currentUser(principal).getId(), "/budgets");
        return "budgets/form";
    }

    @PostMapping
    public String create(@AuthenticationPrincipal UserDetails principal,
                         @Valid @ModelAttribute("budget") BudgetForm budget,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        Long userId = currentUser(principal).getId();
        if (bindingResult.hasErrors()) {
            addFormAttributes(model, budget, userId, "/budgets");
            return "budgets/form";
        }

        financeCoreClient.createBudget(new FinanceCoreClient.BudgetCreateDto(budget.getCategoryId(), budget.getMaxLimit()));
        redirectAttributes.addFlashAttribute("successMessage", "Budget created successfully.");
        return "redirect:/budgets";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@AuthenticationPrincipal UserDetails principal, @PathVariable Long id, Model model) {
        var budget = financeCoreClient.getBudget(id);
        BudgetForm form = toForm(budget);
        addFormAttributes(model, form, currentUser(principal).getId(), "/budgets/" + id);
        return "budgets/form";
    }

    @PostMapping("/{id}")
    public String update(@AuthenticationPrincipal UserDetails principal,
                         @PathVariable Long id,
                         @Valid @ModelAttribute("budget") BudgetForm budget,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        Long userId = currentUser(principal).getId();
        if (bindingResult.hasErrors()) {
            budget.setId(id);
            addFormAttributes(model, budget, userId, "/budgets/" + id);
            return "budgets/form";
        }

        financeCoreClient.updateBudget(id, new FinanceCoreClient.BudgetUpdateDto(budget.getMaxLimit()));
        redirectAttributes.addFlashAttribute("successMessage", "Budget updated successfully.");
        return "redirect:/budgets";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        financeCoreClient.deleteBudget(id);
        redirectAttributes.addFlashAttribute("successMessage", "Budget deleted.");
        return "redirect:/budgets";
    }

    private void addFormAttributes(Model model, BudgetForm budget, Long userId, String formAction) {
        model.addAttribute("budget", budget);
        model.addAttribute("categories", financeCoreClient.getCategories(userId, 0, 1000, "name", "asc").content());
        model.addAttribute("selectedCategoryId", budget.getCategoryId());
        model.addAttribute("formAction", formAction);
    }

    private BudgetForm toForm(FinanceCoreClient.BudgetDto budget) {
        Long categoryId = budget.category() == null ? null : budget.category().id();
        BigDecimal spending = budget.currentSpending() == null ? BigDecimal.ZERO : budget.currentSpending();
        return new BudgetForm(budget.id(), categoryId, budget.maxLimit(), spending, budget.category());
    }

    private User currentUser(UserDetails principal) {
        if (principal == null) {
            throw new ResourceNotFoundException("Authenticated user not found");
        }
        return userService.getUserByEmail(principal.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + principal.getUsername()));
    }
}
