package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Organizer;

public interface OrganizerRepository extends JpaRepository<Organizer, Long> {
    // メールアドレスで主催者を検索する（ログイン時に使用）
    Organizer findByEmail(String email);
}