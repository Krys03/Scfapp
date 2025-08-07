package com.supplychain.scfapp.controller;

import com.supplychain.scfapp.model.Role;
import com.supplychain.scfapp.model.User;
import com.supplychain.scfapp.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class AdminController {

    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/invite")
    @PreAuthorize("hasRole('ADMIN')")
    public User inviteUser(@RequestParam String email, @RequestParam Role role) {
        return userService.inviteUser(email, role);
    }
}
