package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.dto.OrganizerSignupForm;
import com.example.demo.entity.Organizer;
import com.example.demo.repository.OrganizerRepository;

@Service
public class OrganizerService {

    @Autowired
    private OrganizerRepository organizerRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // --- 新規登録 ---
    public void createOrganizer(OrganizerSignupForm form) {
        Organizer org = new Organizer();
        org.setName(form.getName());
        org.setEmail(form.getEmail());
        // パスワードを暗号化して保存
        org.setPassword(passwordEncoder.encode(form.getPassword()));
        organizerRepo.save(org);
    }

    // --- ログイン認証 ---
    // 成功したら Organizer エンティティを返し、失敗したら null を返す
    public Organizer login(String email, String rawPassword) {
        Organizer org = organizerRepo.findByEmail(email);
        if (org == null) {
            return null; // ユーザーがいない
        }
        // パスワード照合
        if (passwordEncoder.matches(rawPassword, org.getPassword())) {
            return org; // 成功
        }
        return null; // パスワード間違い
    }
}