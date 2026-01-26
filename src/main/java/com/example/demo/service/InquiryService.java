package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.example.demo.dto.InquiryForm;

@Service
public class InquiryService {

    @Autowired
    private JavaMailSender mailSender;

    // あなたのGmailアドレス（送信元・管理者宛てとして使用）
    private static final String SENDER_EMAIL = "jt160328@gmail.com";

    /**
     * ① サイト管理者への問い合わせ送信（これが足りていませんでした！）
     */
    public void sendInquiry(InquiryForm form) {
        SimpleMailMessage msg = new SimpleMailMessage();

        // 宛先：サイト管理者（＝自分）に送る
        msg.setTo(SENDER_EMAIL);

        // 送信元
        msg.setFrom(SENDER_EMAIL);

        // 件名
        msg.setSubject("【サイト問い合わせ】" + form.getName() + "様より");

        // 本文
        String text = """
                サイト管理者へのお問い合わせが届きました。

                ■お名前: %s
                ■メール: %s

                ■内容:
                %s
                """.formatted(form.getName(), form.getEmail(), form.getContent());

        msg.setText(text);
        msg.setReplyTo(form.getEmail()); // 返信先を問い合わせた人のアドレスに設定

        try {
            mailSender.send(msg);
            System.out.println("管理者へ問い合わせメールを送信しました");
        } catch (Exception e) {
            System.err.println("メール送信失敗: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * ② 大会主催者への問い合わせ送信
     */
    public void sendInquiryToOrganizer(InquiryForm form, String organizerEmail, String eventName) {
        SimpleMailMessage msg = new SimpleMailMessage();

        // 宛先：大会主催者
        msg.setTo(organizerEmail);

        // 送信元
        msg.setFrom(SENDER_EMAIL);

        // 件名
        msg.setSubject("【大会問い合わせ】" + eventName);

        // 本文
        String text = """
                大会に関するお問い合わせが届きました。

                ■差出人: %s 様 (%s)
                ■大会名: %s

                ■内容:
                %s

                --------------------------------------------------
                ※このメールに返信すると、差出人へ返信されます。
                """.formatted(form.getName(), form.getEmail(), eventName, form.getContent());

        msg.setText(text);
        msg.setReplyTo(form.getEmail());

        try {
            mailSender.send(msg);
            System.out.println("主催者へ問い合わせメールを送信しました To: " + organizerEmail);
        } catch (Exception e) {
            System.err.println("メール送信失敗: " + e.getMessage());
            e.printStackTrace();
        }
    }
}