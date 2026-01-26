package com.example.demo.service;

import org.springframework.stereotype.Service;

@Service
public class MailService {

    public boolean sendMail(String to, String subject, String body, String replyTo) {

        System.out.println("=== MAIL ===");
        System.out.println("TO: " + to);
        System.out.println("SUBJECT: " + subject);
        System.out.println("BODY: " + body);
        System.out.println("============");

        return true;
    }
}
