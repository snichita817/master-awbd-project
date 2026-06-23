package com.awbd.financetracker.controllers;

import com.awbd.financetracker.client.FinanceCoreClient;
import com.awbd.financetracker.dto.CategoryForm;
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

@Controller
@RequestMapping("/categories")
public class CategoryViewController {

    private final FinanceCoreClient financeCoreClient;
    private final UserService userService;

    public CategoryViewController(FinanceCoreClient financeCoreClient, UserService userService) {
        this.financeCoreClient = financeCoreClient;
        this.userService = userService;
    }

    @GetMapping
    public String list(@AuthenticationPrincipal UserDetails principal,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "5") int size,
                       @RequestParam(defaultValue = "name") String sort,
                       @RequestParam(defaultValue = "asc") String dir,
                       Model model) {
        var categoryPage = financeCoreClient.getCategories(currentUser(principal).getId(), page, size, sort, dir);
        model.addAttribute("categoryPage", categoryPage);
        model.addAttribute("categories", categoryPage.content());
        model.addAttribute("categoriesDataUnavailable", categoryPage.isDataUnavailable());
        model.addAttribute("currentPage", page);
        model.addAttribute("currentSize", size);
        model.addAttribute("currentSort", sort);
        model.addAttribute("currentDir", dir);
        model.addAttribute("reverseDir", dir.equalsIgnoreCase("asc") ? "desc" : "asc");
        return "categories/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("category", new CategoryForm());
        model.addAttribute("formAction", "/categories");
        return "categories/form";
    }

    @PostMapping
    public String create(@AuthenticationPrincipal UserDetails principal,
                         @Valid @ModelAttribute("category") CategoryForm category,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("formAction", "/categories");
            return "categories/form";
        }

        financeCoreClient.createCategory(
                currentUser(principal).getId(),
                new FinanceCoreClient.CategoryUpsertDto(category.getName(), category.getDescription())
        );
        redirectAttributes.addFlashAttribute("successMessage", "Category created successfully.");
        return "redirect:/categories";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        FinanceCoreClient.CategoryDto category = financeCoreClient.getCategory(id);
        model.addAttribute("category", new CategoryForm(category.id(), category.name(), category.description()));
        model.addAttribute("formAction", "/categories/" + id);
        return "categories/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("category") CategoryForm category,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            category.setId(id);
            model.addAttribute("formAction", "/categories/" + id);
            return "categories/form";
        }

        financeCoreClient.updateCategory(id, new FinanceCoreClient.CategoryUpsertDto(category.getName(), category.getDescription()));
        redirectAttributes.addFlashAttribute("successMessage", "Category updated successfully.");
        return "redirect:/categories";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        financeCoreClient.deleteCategory(id);
        redirectAttributes.addFlashAttribute("successMessage", "Category deleted.");
        return "redirect:/categories";
    }

    private User currentUser(UserDetails principal) {
        if (principal == null) {
            throw new ResourceNotFoundException("Authenticated user not found");
        }
        return userService.getUserByEmail(principal.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + principal.getUsername()));
    }
}
