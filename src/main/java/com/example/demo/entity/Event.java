package com.example.demo.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.example.demo.domain.EntryType;
import com.example.demo.domain.EventStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "event")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String description;

    /** 大会名 */
    @Column(nullable = false)
    private String name;

    /** 主催者 */
    @ManyToOne
    @JoinColumn(name = "organizer_id")
    private Organizer organizer;

    /** 開催日（Tournament.date を統合） */
    @Column(nullable = false)
    private LocalDate date;

    /** 種別（SINGLE / DOUBLE / TEAM） */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EntryType type;

    /** 団体人数（任意） */
    private Integer teamSizeMin;
    private Integer teamSizeMax;

    /** 定員 */
    private Integer capacity;

    /** 申込締切 */
    private LocalDateTime deadline;

    /** OPEN / CLOSED */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus status = EventStatus.OPEN;
    @Column(name = "is_bracket_public", nullable = false)
    private boolean isBracketPublic = false;

    public boolean isBracketPublic() {
        return isBracketPublic;
    }

    public void setBracketPublic(boolean isBracketPublic) {
        this.isBracketPublic = isBracketPublic;
    }

    // --- getter / setter ---
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Organizer getOrganizer() {
        return organizer;
    }

    public void setOrganizer(Organizer organizer) {
        this.organizer = organizer;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public EntryType getType() {
        return type;
    }

    public void setType(EntryType type) {
        this.type = type;
    }

    public Integer getTeamSizeMin() {
        return teamSizeMin;
    }

    public void setTeamSizeMin(Integer teamSizeMin) {
        this.teamSizeMin = teamSizeMin;
    }

    public Integer getTeamSizeMax() {
        return teamSizeMax;
    }

    public void setTeamSizeMax(Integer teamSizeMax) {
        this.teamSizeMax = teamSizeMax;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public LocalDateTime getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
    }

    public EventStatus getStatus() {
        return status;
    }

    public void setStatus(EventStatus status) {
        this.status = status;
    }

    // ▼▼▼▼▼ ここを追加しました！ ▼▼▼▼▼
    /**
     * 受付終了判定
     * HTML側で ${event.isClosed} と書くとこれが呼ばれます
     */
    public boolean getIsClosed() {
        // 1. ステータスがCLOSEDなら終了
        if (this.status == EventStatus.CLOSED) {
            return true;
        }
        // 2. 締切日時を過ぎていたら終了
        if (this.deadline != null && LocalDateTime.now().isAfter(this.deadline)) {
            return true;
        }
        return false;
    }
    // ▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲
}