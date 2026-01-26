package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank; // ★これを追加

public class MemberDto {

    private String teamName;

    // ★名前は必須にする
    @NotBlank(message = "名前を入力してください")
    private String name;

    // ★所属は「団体戦」のときに消えるので、チェックをつけない（任意にする）
    private String affiliation;

    // ★性別は必須にする
    @NotBlank(message = "性別を選択してください")
    private String gender;

    // --- 以下、Getter/Setter はそのままでOK ---

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAffiliation() {
        return affiliation;
    }

    public void setAffiliation(String affiliation) {
        this.affiliation = affiliation;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }
}