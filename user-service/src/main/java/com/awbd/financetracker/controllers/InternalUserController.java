package com.awbd.financetracker.controllers;

import com.awbd.financetracker.dto.UserSummaryDto;
import com.awbd.financetracker.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/internal/users")
public class InternalUserController {

    private final UserService userService;

    public InternalUserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserSummaryDto> getById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(UserSummaryDto::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/summary")
    public ResponseEntity<UserSummaryDto> getSummary(@PathVariable Long id) {
        return getById(id);
    }

    @GetMapping("/by-email")
    public ResponseEntity<UserSummaryDto> getByEmail(@RequestParam String email) {
        return userService.getUserByEmail(email)
                .map(UserSummaryDto::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserSummaryDto>> search(@RequestParam(defaultValue = "") String query) {
        List<UserSummaryDto> users = userService.searchUsers(query).stream()
                .map(UserSummaryDto::from)
                .toList();
        return ResponseEntity.ok(users);
    }
}
