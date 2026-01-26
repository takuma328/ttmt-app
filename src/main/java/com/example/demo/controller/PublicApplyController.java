package com.example.demo.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.domain.EntryType;
import com.example.demo.dto.ApplyForm;
import com.example.demo.dto.InquiryForm; // ★必要
import com.example.demo.dto.MemberDto;
import com.example.demo.entity.Event;
import com.example.demo.entity.Match;
import com.example.demo.repository.EntryMemberRepository;
import com.example.demo.repository.EntryRepository;
import com.example.demo.repository.EventRepository;
import com.example.demo.repository.MatchRepository;
import com.example.demo.service.EntryValidator;
import com.example.demo.service.InquiryService; // ★必要
import com.example.demo.service.PublicApplyService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/public/events") // ★重要：ここがURLのベースです
public class PublicApplyController {

    private final EventRepository eventRepo;
    private final EntryRepository entryRepo;
    private final EntryMemberRepository memberRepo;
    private final PublicApplyService applyService;
    private final EntryValidator validator;
    private final MatchRepository matchRepo;
    private final InquiryService inquiryService; // ★追加

    // コンストラクタ
    public PublicApplyController(
            EventRepository eventRepo,
            EntryRepository entryRepo,
            EntryMemberRepository memberRepo,
            PublicApplyService applyService,
            EntryValidator validator,
            MatchRepository matchRepo,
            InquiryService inquiryService) { // ★追加

        this.eventRepo = eventRepo;
        this.entryRepo = entryRepo;
        this.memberRepo = memberRepo;
        this.applyService = applyService;
        this.validator = validator;
        this.matchRepo = matchRepo;
        this.inquiryService = inquiryService; // ★追加
    }

    // --- 既存のメソッド ---

    @GetMapping("/{eventId}/apply")
    public String showForm(@PathVariable Long eventId, Model model) {
        Event event = eventRepo.findById(eventId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (isFull(event))
            return "redirect:/public/events/" + eventId + "/detail";
        ApplyForm form = ApplyForm.empty();
        form.setType(event.getType());
        model.addAttribute("event", event);
        model.addAttribute("applyForm", form);
        return "public/events/apply_form";
    }

    @PostMapping("/{eventId}/apply")
    public String apply(@PathVariable Long eventId, @Valid @ModelAttribute("applyForm") ApplyForm form,
            BindingResult br, Model model) {
        Event event = eventRepo.findById(eventId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (isFull(event))
            return "redirect:/public/events/" + eventId + "/detail";
        if (br.hasErrors()) {
            model.addAttribute("event", event);
            return "public/events/apply_form";
        }

        var memberNames = form.getMembers().stream().map(MemberDto::getName).toList();
        try {
            validator.validatePublic(event, memberNames);
        } catch (Exception e) {
            br.reject("entryError", e.getMessage());
            model.addAttribute("event", event);
            return "public/events/apply_form";
        }

        Long entryId = applyService.apply(event, form);
        applyService.sendReceiptEmail(event, form, entryId);
        return "redirect:/public/events/" + eventId + "/apply/complete?entryId=" + entryId;
    }

    @GetMapping("/{eventId}/apply/complete")
    public String complete(@RequestParam Long entryId, Model model) {
        var entry = entryRepo.findById(entryId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("entry", entry);
        return "public/events/complete";
    }

    @GetMapping("/{eventId}/detail")
    public String detail(@PathVariable Long eventId, Model model) {
        Event event = eventRepo.findById(eventId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        long entryCount = getCurrentCount(event);
        boolean closed = LocalDateTime.now().isAfter(event.getDeadline()) || isFull(event);
        model.addAttribute("event", event);
        model.addAttribute("entryCount", entryCount);
        model.addAttribute("isClosed", closed);
        return "public/events/detail";
    }

    @GetMapping("/{eventId}/bracket")
    public String showBracket(@PathVariable Long eventId, Model model) {
        Event event = eventRepo.findById(eventId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!event.isBracketPublic())
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "非公開");
        List<Match> matches = matchRepo.findByEventOrderByRoundAscMatchNoAsc(event);
        Map<Integer, List<Match>> roundMap = matches.stream()
                .collect(Collectors.groupingBy(Match::getRound, TreeMap::new, Collectors.toList()));
        model.addAttribute("event", event);
        model.addAttribute("roundMap", roundMap);
        return "public/events/bracket";
    }

    // ▼▼▼▼▼▼▼▼▼▼ 今回追加する問い合わせ機能 ▼▼▼▼▼▼▼▼▼▼

    /** 1. 問い合わせフォーム表示 */
    @GetMapping("/{eventId}/inquiry")
    public String inquiryForm(@PathVariable Long eventId, Model model) {
        Event event = eventRepo.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        model.addAttribute("event", event);
        model.addAttribute("inquiryForm", new InquiryForm());

        return "public/events/inquiry"; // inquiry.html を表示
    }

    /** 2. 問い合わせ送信処理 */
    @PostMapping("/{eventId}/inquiry")
    public String sendInquiry(
            @PathVariable Long eventId,
            @Valid @ModelAttribute InquiryForm form,
            BindingResult br,
            Model model) {

        Event event = eventRepo.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (br.hasErrors()) {
            model.addAttribute("event", event);
            return "public/events/inquiry";
        }

        // 主催者へメール送信
        String organizerEmail = event.getOrganizer().getEmail();
        inquiryService.sendInquiryToOrganizer(form, organizerEmail, event.getName());

        return "redirect:/public/events/" + eventId + "/inquiry/complete";
    }

    /** 3. 問い合わせ完了画面 */
    @GetMapping("/{eventId}/inquiry/complete")
    public String inquiryComplete(@PathVariable Long eventId, Model model) {
        Event event = eventRepo.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("event", event);
        return "public/events/inquiry_complete";
    }
    // ▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲

    private long getCurrentCount(Event event) {
        if (event.getType() == EntryType.TEAM) {
            return entryRepo.countByEvent(event);
        } else {
            return memberRepo.countByEntryEvent(event);
        }
    }

    private boolean isFull(Event event) {
        if (event.getCapacity() == null || event.getCapacity() == 0)
            return false;
        return getCurrentCount(event) >= event.getCapacity();
    }

    // トップページ

}