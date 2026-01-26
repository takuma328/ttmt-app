package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.entity.EntryMember;
import com.example.demo.entity.Event;
//二郎

public interface EntryMemberRepository extends JpaRepository<EntryMember, Long> {

    // 個人戦用：Entry経由でEventを条件にし、EntryID順で並べる
    @Query("SELECT m FROM EntryMember m WHERE m.entry.event = :event ORDER BY m.entry.id ASC")
    List<EntryMember> findSinglesOrderByEntryId(@Param("event") Event event);

    // 団体戦用：チーム名でソートしたい場合
    // ★重要: m.teamName は無いので m.entry.teamName を見る
    @Query("SELECT m FROM EntryMember m WHERE m.entry.event = :event ORDER BY m.entry.teamName ASC, m.id ASC")
    List<EntryMember> findTeamsOrderByTeamName(@Param("event") Event event);

    int countByEntryEvent(Event event);
}