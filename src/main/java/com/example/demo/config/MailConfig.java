package com.example.demo.config;

import java.io.InputStream;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty; // ← 変更
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;

@Configuration
public class MailConfig {

    /**
     * 'spring.mail.host' プロパティが未設定の場合（matchIfMissing = true）
     * または、特定の値（havingValue）の場合にのみ登録されるダミー実装。
     * * これにより、application.properties に spring.mail.host が正しく設定されている場合、
     * このBeanは作成されず、Spring Boot 自動設定の「本物の」JavaMailSenderが使われるようになります。
     */
    @Bean
    @Primary
    // ↓↓↓ このアノテーションを丸ごと変更しました ↓↓↓
    @ConditionalOnProperty(name = "spring.mail.host", matchIfMissing = true, havingValue = "dummy-value-for-noop-sender")
    public JavaMailSender noopMailSender() {
        return new JavaMailSender() {
            @Override
            public MimeMessage createMimeMessage() {
                // Session なしで生成（No-op でも呼び出し側が null で死なないように）
                return new MimeMessage((Session) null);
            }

            @Override
            public MimeMessage createMimeMessage(InputStream contentStream) throws MailException {
                try {
                    return new MimeMessage((Session) null, contentStream);
                } catch (Exception e) {
                    throw new MailException("Failed to create MimeMessage from stream", e) {
                        private static final long serialVersionUID = 1L;
                    };
                }
            }

            @Override
            public void send(MimeMessage mimeMessage) throws MailException {
                // No-op
            }

            @Override
            public void send(MimeMessage... mimeMessages) throws MailException {
                // No-op
            }

            @Override
            public void send(MimeMessagePreparator mimeMessagePreparator) throws MailException {
                // No-op
            }

            @Override
            public void send(MimeMessagePreparator... mimeMessagePreparators) throws MailException {
                // No-op
            }

            @Override
            public void send(SimpleMailMessage simpleMessage) throws MailException {
                // No-op
            }

            @Override
            public void send(SimpleMailMessage... simpleMessages) throws MailException {
                // No-op
            }
        };
    }
}