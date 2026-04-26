package com.example.eventsphere.controller;

import com.example.eventsphere.enums.UserRole;
import com.example.eventsphere.model.User;
import com.example.eventsphere.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class loginController {

    private final UserService userService;

    @GetMapping({"/", "/login"})
    public String login(@RequestParam(required = false) String error,
                        @RequestParam(required = false) String logout,
                        Model model) {
        if (error  != null) model.addAttribute("errorMsg",  "Invalid email or password.");
        if (logout != null) model.addAttribute("logoutMsg", "You have been logged out.");
        return "login/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new User());
        return "login/register";
    }

    @PostMapping("/register")
    public String doRegister(@ModelAttribute User user,
                             @RequestParam String confirmPassword,
                             @RequestParam(defaultValue = "USER") String role,
                             RedirectAttributes ra) {
        if (!user.getPassword().equals(confirmPassword)) {
            ra.addFlashAttribute("regError", "Passwords do not match.");
            return "redirect:/register";
        }
        if (userService.emailExists(user.getEmail())) {
            ra.addFlashAttribute("regError", "Email already registered.");
            return "redirect:/register";
        }
        user.setRole(UserRole.valueOf(role));
        userService.register(user);
        ra.addFlashAttribute("regSuccess", "Account created! Please log in.");
        return "redirect:/login";
    }
}
