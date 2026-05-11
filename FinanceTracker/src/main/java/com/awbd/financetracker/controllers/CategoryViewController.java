package com.awbd.financetracker.controllers;

import com.awbd.financetracker.entity.Category;
import com.awbd.financetracker.exception.DuplicateResourceException;
import com.awbd.financetracker.exception.ResourceNotFoundException;
import com.awbd.financetracker.service.CategoryService;
import com.awbd.financetracker.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/categories")
public class CategoryViewController {

    private final CategoryService categoryService;
    private final UserService userService;

    public CategoryViewController(CategoryService categoryService, UserService userService) {
        this.categoryService = categoryService;
        this.userService = userService;
    }

    @GetMapping
    public String list(@AuthenticationPrincipal UserDetails principal,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "5") int size,
                       @RequestParam(defaultValue = "name") String sort,
                       @RequestParam(defaultValue = "asc") String dir,
                       Model model) {
        Sort.Direction direction = dir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sort));
        userService.getUserByEmail(principal.getUsername()).ifPresent(user -> {
            Page<Category> categoryPage = categoryService.getCategoriesByUserId(user.getId(), pageRequest);
            model.addAttribute("categories", categoryPage.getContent());
            model.addAttribute("categoryPage", categoryPage);
            model.addAttribute("userId", user.getId());
        });
        model.addAttribute("currentSort", sort);
        model.addAttribute("currentDir", dir);
        model.addAttribute("reverseDir", dir.equalsIgnoreCase("asc") ? "desc" : "asc");
        model.addAttribute("currentSize", size);
        return "categories/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("category", new Category());
        model.addAttribute("formAction", "/categories");
        return "categories/form";
    }

    @PostMapping
    public String create(@AuthenticationPrincipal UserDetails principal,
                         @Valid @ModelAttribute("category") Category category,
                         BindingResult result,
                         RedirectAttributes redirectAttrs,
                         Model model) {
        if (result.hasErrors()) {
            model.addAttribute("formAction", "/categories");
            return "categories/form";
        }
        userService.getUserByEmail(principal.getUsername()).ifPresent(user -> {
            try {
                categoryService.createCategory(user.getId(), category);
            } catch (DuplicateResourceException ex) {
                result.rejectValue("name", "duplicate", ex.getMessage());
            }
        });
        if (result.hasErrors()) {
            model.addAttribute("formAction", "/categories");
            return "categories/form";
        }
        redirectAttrs.addFlashAttribute("successMessage", "Category created successfully.");
        return "redirect:/categories";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Category category = categoryService.getCategoryById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
        model.addAttribute("category", category);
        model.addAttribute("formAction", "/categories/" + id);
        return "categories/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("category") Category category,
                         BindingResult result,
                         RedirectAttributes redirectAttrs,
                         Model model) {
        if (result.hasErrors()) {
            model.addAttribute("formAction", "/categories/" + id);
            return "categories/form";
        }
        try {
            categoryService.updateCategory(id, category);
        } catch (DuplicateResourceException ex) {
            result.rejectValue("name", "duplicate", ex.getMessage());
            model.addAttribute("formAction", "/categories/" + id);
            return "categories/form";
        }
        redirectAttrs.addFlashAttribute("successMessage", "Category updated successfully.");
        return "redirect:/categories";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        categoryService.deleteCategory(id);
        redirectAttrs.addFlashAttribute("successMessage", "Category deleted.");
        return "redirect:/categories";
    }
}
