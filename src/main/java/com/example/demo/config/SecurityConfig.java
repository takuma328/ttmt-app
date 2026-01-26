package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; // ★追加
import org.springframework.security.crypto.password.PasswordEncoder; // ★追加
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // ... (中身はあなたの提示したコードのままでOK) ...
        // .requestMatchers("/organizer/**").permitAll() があるので、signupもloginも通ります。

        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/home", "/events/**", "/inquiry/**", "/css/**", "/js/**").permitAll() // inquiryなども許可
                        .requestMatchers("/organizer/**").permitAll() // これで主催者系は全部OK
                        .requestMatchers("/admin/login").permitAll()
                        .requestMatchers("/admin/**").authenticated()
                        .anyRequest().permitAll())
        // ... (formLogin, logout 設定などそのまま) ...
        ;

        return http.build();
    }

    // ★★★ これを追加してください（パスワードの暗号化に使います） ★★★
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}