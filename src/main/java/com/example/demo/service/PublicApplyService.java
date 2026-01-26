package com.example.demo.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.ApplyForm;
import com.example.demo.dto.MemberDto;
import com.example.demo.entity.Entry;
import com.example.demo.entity.EntryMember;
import com.example.demo.entity.Event;
import com.example.demo.enums.Gender;
import com.example.demo.repository.EntryMemberRepository;
import com.example.demo.repository.EntryRepository;

@Service
public class PublicApplyService {

    private final EntryRepository entryRepo;
    private final EntryMemberRepository memberRepo;
    private final JavaMailSender mailSender;

    // ★ InquiryServiceと同じアドレスを使用（これが動く実績あり）
    private static final String SENDER_EMAIL = "jt160328@gmail.com";

    // コンストラクタインジェクション（推奨される書き方です。このままでOK）
    public PublicApplyService(
            EntryRepository entryRepo,
            EntryMemberRepository memberRepo,
            JavaMailSender mailSender) {
        this.entryRepo = entryRepo;
        this.memberRepo = memberRepo;
        this.mailSender = mailSender;
    }

    @Transactional
    public Long apply(Event event, ApplyForm form) {
        // --- ここは変更なし ---
        Entry entry = new Entry();
        entry.setEvent(event);
        entry.setType(event.getType());
        entry.setContactName(form.getContactName());
        entry.setContactEmail(form.getContactEmail());
        entry.setPhone(form.getPhone());
        entry.setTeamName(form.getTeamName());
        entry.setStatus("REQUESTED");

        Entry saved = entryRepo.save(entry);

        for (MemberDto mDto : form.getMembers()) {
            if (mDto.getName() == null || mDto.getName().isBlank())
                continue;

            EntryMember m = new EntryMember();
            m.setEntry(saved);
            m.setName(mDto.getName());
            m.setAffiliation(mDto.getAffiliation());

            if (mDto.getGender() != null && !mDto.getGender().isEmpty()) {
                m.setGender(Gender.valueOf(mDto.getGender()));
            }
            memberRepo.save(m);
        }
        return saved.getId();
    }

    /**
     * 申込完了メール送信
     */
    public void sendReceiptEmail(Event event, ApplyForm form, Long entryId) {
        // 件名
        String subject = "【TTMT】大会申し込み完了のお知らせ";

        // 本文（InquiryServiceと同じ形式で記述）
        String body = """
                %s 様

                大会へのお申込みを受け付けました。
                以下の内容で登録いたしました。

                ■大会名: %s
                ■開催日: %s
                ■種別: %s
                ■受付番号: %d

                当日は気をつけてお越しください。

                ※本メールは送信専用アドレスから送信されています。
                """.formatted(
                form.getContactName(),
                event.getName(),
                event.getDate(), // 日付もあったほうが親切です
                event.getType(),
                entryId);

        // メール作成
        SimpleMailMessage msg = new SimpleMailMessage();

        // 宛先：申込フォームに入力された連絡先メールアドレス
        msg.setTo(form.getContactEmail());

        // 送信元：InquiryServiceと同じGmailアドレス
        msg.setFrom(SENDER_EMAIL);

        // 件名・本文
        msg.setSubject(subject);
        msg.setText(body);

        // 送信実行
        try {
            mailSender.send(msg);
            System.out.println("申込完了メールを送信しました To: " + form.getContactEmail());
        } catch (Exception e) {
            // エラーログを出力（アプリは停止させない）
            System.err.println("メール送信エラー: " + e.getMessage());
            e.printStackTrace();
        }
    }
}