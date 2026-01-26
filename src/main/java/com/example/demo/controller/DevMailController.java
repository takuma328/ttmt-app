// src/main/java/com/example/demo/controller/DevMailController.java
package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.service.MailService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class DevMailController {

    private final MailService mail;

    // 例: http://localhost:8080/dev/mail?to=foo@example.com
    @GetMapping("/dev/mail")
    public ResponseEntity<String> test(@RequestParam(required = false) String to) {
        if (to == null || to.isBlank()) {
            return ResponseEntity.badRequest()
                    .body("NG: パラメータ to を付けてください。例 /dev/mail?to=foo@example.com");
        }
        boolean ok = mail.sendMail(to, "[TTMT] テスト送信", "これはテストメールです。", to);
        return ok ? ResponseEntity.ok("OK: sent to " + to)
                : ResponseEntity.internalServerError().body("NG: send failed (ログを参照)");

    }
}
