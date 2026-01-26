package com.example.demo.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping; // ★追加
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.domain.EntryType;
import com.example.demo.entity.Entry;
import com.example.demo.entity.EntryMember;
import com.example.demo.entity.Event;
import com.example.demo.repository.EntryMemberRepository;
import com.example.demo.repository.EntryRepository;
import com.example.demo.repository.EventRepository;

@Controller
public class OrganizerEntryController {

    private final EventRepository eventRepo;
    private final EntryMemberRepository memberRepo;
    private final EntryRepository entryRepo;

    public OrganizerEntryController(
            EventRepository eventRepo,
            EntryMemberRepository memberRepo,
            EntryRepository entryRepo) {
        this.eventRepo = eventRepo;
        this.memberRepo = memberRepo;
        this.entryRepo = entryRepo;
    }

    /** 参加者一覧 */
    @GetMapping("/organizer/events/{eventId}/entries")
    public String entries(@PathVariable Long eventId, Model model) {

        Event event = eventRepo.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        model.addAttribute("event", event);

        if (event.getType() == EntryType.TEAM) {
            // 団体戦
            List<Entry> teams = entryRepo.findByEventIdOrderByCreatedAtAsc(eventId);
            model.addAttribute("teams", teams);
        } else {
            // 個人戦
            List<EntryMember> singles = memberRepo.findSinglesOrderByEntryId(event);
            model.addAttribute("singles", singles);
        }

        return "organizer/events/entries";
    }

    /** 大会詳細 */
    @GetMapping("/organizer/events/{eventId}/detail")
    public String organizerDetail(@PathVariable Long eventId, Model model) {
        Event event = eventRepo.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        long entryCount = entryRepo.countByEvent(event);

        model.addAttribute("event", event);
        model.addAttribute("entryCount", entryCount);

        return "organizer/events/detail";
    }

    // ▼▼▼▼▼ 追加：参加取り消し（棄権）機能 ▼▼▼▼▼
    @PostMapping("/organizer/events/{eventId}/entries/{entryId}/delete")
    public String deleteEntry(@PathVariable Long eventId, @PathVariable Long entryId) {
        // エントリーIDを指定して削除（関連するメンバーも自動で消える設定になっているはずです）
        entryRepo.deleteById(entryId);

        // 一覧画面に戻る
        return "redirect:/organizer/events/" + eventId + "/entries";
    }
}