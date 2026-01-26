package com.example.demo.service;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.demo.entity.Admin;
import com.example.demo.repository.AdminRepository;
import com.example.demo.security.AdminUserDetails;

@Service
public class AdminUserDetailsService implements UserDetailsService {

    private final AdminRepository adminRepo;

    public AdminUserDetailsService(AdminRepository adminRepo) {
        this.adminRepo = adminRepo;
    }

    @Override
    public AdminUserDetails loadUserByUsername(String idText) throws UsernameNotFoundException {

        if (idText == null || idText.isBlank()) {
            throw new UsernameNotFoundException("ID が空です");
        }

        Long id;
        try {
            id = Long.parseLong(idText);
        } catch (NumberFormatException e) {
            throw new UsernameNotFoundException("ID が数字ではありません");
        }

        Admin admin = adminRepo.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("管理者が見つかりません"));

        return new AdminUserDetails(admin);
    }
}
