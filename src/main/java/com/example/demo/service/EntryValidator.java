package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Component;

import com.example.demo.domain.EventStatus;
import com.example.demo.entity.Event;
import com.example.demo.repository.EntryRepository;

@Component
public class EntryValidator {
    private final EntryRepository entryRepository;

    public EntryValidator(EntryRepository entryRepository) {
        this.entryRepository = entryRepository;
    }

    public void validatePublic(Event event, List<String> members) {
        if (event.getStatus() == EventStatus.CLOSED)
            throw new IllegalStateException("受付はクローズされています。");
        if (event.getDeadline() != null && LocalDateTime.now().isAfter(event.getDeadline()))
            throw new IllegalStateException("締切を過ぎています。");
        validateCommon(event, members);
    }

    public void validateAdmin(Event event, List<String> members) {
        validateCommon(event, members); // 締切/OPENは無視
    }

    private void validateCommon(Event event, List<String> members) {
        int n = members == null ? 0 : (int) members.stream().filter(s -> s != null && !s.isBlank()).count();
        if (event.getTeamSizeMin() != null && n < event.getTeamSizeMin())
            throw new IllegalArgumentException("メンバー数が不足しています（最小 " + event.getTeamSizeMin() + " 人）。");
        if (event.getTeamSizeMax() != null && n > event.getTeamSizeMax())
            throw new IllegalArgumentException("メンバー数が多すぎます（最大 " + event.getTeamSizeMax() + " 人）。");

        if (event.getCapacity() != null) {
            long current = entryRepository.countByEvent(event);
            if (current >= event.getCapacity())
                throw new IllegalStateException("定員に達しています。");
        }
    }
}
