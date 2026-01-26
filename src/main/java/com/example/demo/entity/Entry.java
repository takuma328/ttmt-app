package com.example.demo.entity;

import java.time.LocalDateTime;
import java.util.List; // ★追加

import com.example.demo.domain.EntryType;

import jakarta.persistence.CascadeType; // ★追加
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany; // ★追加
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "entry")
public class Entry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event event;

    @Enumerated(EnumType.STRING)
    private EntryType type;

    private String teamName; // チーム名

    // ... 他のフィールド (contactName, phoneなど) ...
    private String contactName;
    private String contactEmail;
    private String phone;
    private String status;
    private LocalDateTime createdAt;

    // ★★★ これを追加してください！ ★★★
    // 「このEntry(チーム)には、複数のMemberがいるよ」という設定です
    @OneToMany(mappedBy = "entry", cascade = CascadeType.ALL)
    private List<EntryMember> members;
    // ★★★★★★★★★★★★★★★★★★★★

    @PrePersist
    public void onPrePersist() {
        this.createdAt = LocalDateTime.now();
    }

    // --- Getter / Setter ---
    // (既存のものはそのまま)

    // ★★★ これも追加 ★★★
    public List<EntryMember> getMembers() {
        return members;
    }

    public void setMembers(List<EntryMember> members) {
        this.members = members;
    }
    // ... 他のGetter/Setter ...

    // (省略されているGetter/Setterはそのまま残してください)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public EntryType getType() {
        return type;
    }

    public void setType(EntryType type) {
        this.type = type;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}