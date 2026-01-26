package com.example.demo.entity;

import com.example.demo.enums.Gender; // さっき作ったEnumをインポート

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class EntryMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ★チーム名は親(Entry)にあるので、ここからは削除しました

    // メンバー名
    private String name;

    // 所属（学校名やクラブ名など、個人ごとの所属があれば）
    private String affiliation;

    // ★Stringではなく、Enumを使うと安全です
    @Enumerated(EnumType.STRING) // DBには "MALE", "FEMALE" という文字で保存されます
    private Gender gender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entry_id") // DBの外部キーカラム名
    private Entry entry;
}