package com.example.demo.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.domain.EntryType;
import com.example.demo.dto.ApplyForm;
import com.example.demo.entity.Event;
import com.example.demo.repository.EntryMemberRepository;
import com.example.demo.repository.EntryRepository;
import com.example.demo.repository.EventRepository;
import com.example.demo.service.PublicApplyService;

@Controller
@RequestMapping("/events")
public class EventController {

    private final EventRepository eventRepo;
    private final EntryRepository entryRepo;
    private final EntryMemberRepository memberRepo;
    private final PublicApplyService applyService;

    public EventController(
            EventRepository eventRepo,
            EntryRepository entryRepo,
            EntryMemberRepository memberRepo,
            PublicApplyService applyService) {
        this.eventRepo = eventRepo;
        this.entryRepo = entryRepo;
        this.memberRepo = memberRepo;
        this.applyService = applyService;
    }

    /** ① 公開中の大会一覧 */
    @GetMapping
    public String list(Model model) {
        List<Event> allEvents = eventRepo.findAll();
        List<Event> futureEvents = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (Event event : allEvents) {
            if (event.getDate() == null)
                continue;
            // 開催日が今日以降ならリストに追加
            if (!event.getDate().isBefore(today)) {
                futureEvents.add(event);
            }
        }
        futureEvents.sort((a, b) -> a.getDate().compareTo(b.getDate()));
        model.addAttribute("events", futureEvents);
        return "public/events/list";
    }

    /** ② 大会詳細画面 */
    @GetMapping("/{eventId}/detail")
    public String detail(@PathVariable Long eventId, Model model) {
        Event event = eventRepo.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        model.addAttribute("event", event);

        // ★修正: 締切判定（日付が過ぎている OR 定員オーバーなら true）
        boolean closed = isDeadlinePassed(event) || isFull(event);
        model.addAttribute("isClosed", closed);

        // 現在人数の表示用
        model.addAttribute("entryCount", getCurrentCount(event));

        return "public/events/detail";
    }

    /** ③ 申込フォーム画面 (GET) */
    @GetMapping("/{eventId}/apply")
    public String showApplyForm(@PathVariable Long eventId, Model model) {
        Event event = eventRepo.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        // ★修正: 締切 OR 満員なら詳細画面へ戻す
        if (isDeadlinePassed(event) || isFull(event)) {
            return "redirect:/events/" + eventId + "/detail";
        }

        model.addAttribute("event", event);
        model.addAttribute("applyForm", ApplyForm.empty());

        return "public/events/apply_form";
    }

    /** ④ 申込送信処理 (POST) */
    @PostMapping("/{eventId}/apply")
    public String processApply(
            @PathVariable Long eventId,
            @ModelAttribute("applyForm") @Validated ApplyForm form,
            BindingResult result,
            Model model) {

        Event event = eventRepo.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        // ★修正: 締切 OR 満員なら弾く
        if (isDeadlinePassed(event) || isFull(event)) {
            return "redirect:/events/" + eventId + "/detail";
        }

        if (result.hasErrors()) {
            model.addAttribute("event", event);
            return "public/events/apply_form";
        }

        Long entryId = applyService.apply(event, form);
        return "redirect:/events/" + eventId + "/complete?entryId=" + entryId;
    }

    /** ⑤ 完了画面 */
    @GetMapping("/{eventId}/complete")
    public String complete(
            @PathVariable Long eventId,
            @RequestParam Long entryId,
            Model model) {

        Event event = eventRepo.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        com.example.demo.entity.Entry entry = entryRepo.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Entry not found"));

        model.addAttribute("event", event);
        model.addAttribute("entry", entry);

        return "public/events/complete";
    }

    // --- プライベートメソッド ---

    /** 締切日チェック */
    private boolean isDeadlinePassed(Event event) {
        if (event.getDeadline() == null) {
            return false;
        }
        return event.getDeadline().toLocalDate().isBefore(java.time.LocalDate.now());
    }

    /** ★追加: 現在の参加数を取得 */
    private long getCurrentCount(Event event) {
        long count = 0;

        if (event.getType() == EntryType.TEAM) {
            count = entryRepo.countByEvent(event);
        } else {
            count = memberRepo.countByEntryEvent(event);
        }

        // ★この1行を追加してください！コンソールに数字が出ます
        System.out.println("【DEBUG】大会ID:" + event.getId() + " の参加人数は " + count + " 人です");

        return count;
    }

    /** ★追加: 定員オーバーチェック */
    private boolean isFull(Event event) {
        if (event.getCapacity() == null || event.getCapacity() == 0) {
            return false; // 定員設定なしなら無限
        }
        long current = getCurrentCount(event);
        return current >= event.getCapacity();
    }
}