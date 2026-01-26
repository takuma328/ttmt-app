package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.entity.Admin;
import com.example.demo.repository.AdminRepository;

@Controller
public class AdminLoginController {

    @Autowired
    private AdminRepository adminRepository;

    @GetMapping("/admin/login")
    public String showLogin() {
        return "admin/login";
    }

    @PostMapping("/admin/login")
    public String login(
            @RequestParam Long id,
            @RequestParam String password,
            RedirectAttributes ra) {

        Admin admin = adminRepository.findById(id).orElse(null);

        if (admin != null && admin.getPassword().equals(password)) {
            ra.addFlashAttribute("adminName", admin.getName());
            return "redirect:/admin/dashboard";
        }

        ra.addFlashAttribute("error", "ID またはパスワードが違います");
        return "redirect:/admin/login";
    }

    @GetMapping("/admin/dashboard")
    public String dashboard() {
        return "admin/dashboard";
    }
}
