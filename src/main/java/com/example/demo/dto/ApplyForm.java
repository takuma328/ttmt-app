package com.example.demo.dto;

import java.util.ArrayList;
import java.util.List;

import com.example.demo.domain.EntryType;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ApplyForm {

    @NotNull
    private EntryType type;

    @NotBlank(message = "代表者名は必須です")
    private String contactName;

    @NotBlank(message = "メールは必須です")
    @Email
    private String contactEmail;

    @NotBlank(message = "電話番号は必須です")
    private String phone;

    // ★★★ここに teamName を追加します★★★
    private String teamName;

    // ★ メンバー統合形式
    private List<MemberDto> members = new ArrayList<>();

    public static ApplyForm empty() {
        ApplyForm f = new ApplyForm();
        f.members.add(new MemberDto()); // 初期行1つ
        return f;
    }

    // ===== Getter Setter =====
    public EntryType getType() {
        return type;
    }

    public void setType(EntryType type) {
        this.type = type;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    // ★★★ここに teamName の Getter/Setter を追加します★★★
    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public List<MemberDto> getMembers() {
        return members;
    }

    public void setMembers(List<MemberDto> members) {
        this.members = members;
    }
}