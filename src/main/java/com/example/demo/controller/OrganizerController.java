package com.example.demo.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
import com.example.demo.entity.Entry;
import com.example.demo.entity.Event;
import com.example.demo.entity.Match;
import com.example.demo.entity.Organizer;
import com.example.demo.repository.EntryMemberRepository;
import com.example.demo.repository.EntryRepository;
import com.example.demo.repository.EventRepository;
import com.example.demo.repository.MatchRepository;
import com.example.demo.repository.OrganizerRepository;
import com.example.demo.service.TournamentService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/organizer")
public class OrganizerController {

    private final EventRepository eventRepo;
    private final EntryRepository entryRepo;
    private final EntryMemberRepository memberRepo;
    private final MatchRepository matchRepo;
    private final TournamentService tournamentService;
    private final OrganizerRepository organizerRepo;
    private final HttpSession session;

    public OrganizerController(
            EventRepository eventRepo,
            EntryRepository entryRepo,
            EntryMemberRepository memberRepo,
            MatchRepository matchRepo,
            TournamentService tournamentService,
            OrganizerRepository organizerRepo,
            HttpSession session) {
        this.eventRepo = eventRepo;
        this.entryRepo = entryRepo;
        this.memberRepo = memberRepo;
        this.matchRepo = matchRepo;
        this.tournamentService = tournamentService;
        this.organizerRepo = organizerRepo;
        this.session = session;
    }

    // --- ログイン画面 ---
    @GetMapping("/login")
    public String loginForm() {
        return "organizer/login";
    }

    // --- ログイン処理 ---
    @PostMapping("/login")
    public String login(@RequestParam("email") String email, @RequestParam("password") String password, Model model) {
        Organizer org = organizerRepo.findByEmail(email);
        if (org == null || !new BCryptPasswordEncoder().matches(password, org.getPassword())) {
            model.addAttribute("error", "メールアドレスまたはパスワードが違います");
            return "organizer/login";
        }
        session.setAttribute("organizer", org);
        session.setAttribute("organizerId", org.getId());
        session.setAttribute("organizerName", org.getName());
        return "redirect:/organizer/dashboard";
    }

    // --- ログアウト ---
    @GetMapping("/logout")
    public String logout() {
        session.invalidate();
        return "redirect:/";
    }

    // --- ダッシュボード ---
    @GetMapping("/dashboard")
    public String dashboard() {
        if (session.getAttribute("organizer") == null)
            return "redirect:/organizer/login";
        return "organizer/dashboard";
    }

    // --- 大会一覧 ---
    @GetMapping("/events")
    public String events(Model model) {
        Organizer org = (Organizer) session.getAttribute("organizer");
        if (org == null)
            return "redirect:/organizer/login";

        List<Event> events = eventRepo.findByOrganizer(org);

        Map<Long, Long> counts = new HashMap<>();
        for (Event event : events) {
            long count = 0;
            if (event.getType() == EntryType.TEAM) {
                count = entryRepo.countByEvent(event);
            } else {
                count = memberRepo.countByEntryEvent(event);
            }
            counts.put(event.getId(), count);
        }
        model.addAttribute("counts", counts);
        model.addAttribute("events", events);
        return "organizer/events";
    }

    // --- 大会作成画面 ---
    @GetMapping("/events/new")
    public String newEvent(Model model) {
        if (session.getAttribute("organizer") == null)
            return "redirect:/organizer/login";
        model.addAttribute("event", new Event());
        return "organizer/events/new";
    }

    // --- 大会登録処理 ---
    @PostMapping("/events")
    public String create(@Validated @ModelAttribute Event event, BindingResult result) {
        Organizer org = (Organizer) session.getAttribute("organizer");
        if (org == null)
            return "redirect:/organizer/login";
        if (result.hasErrors())
            return "organizer/events/new";

        event.setOrganizer(org);
        eventRepo.save(event);
        return "redirect:/organizer/events";
    }

    // --- 大会削除処理 ---
    @PostMapping("/events/{id}/delete")
    @org.springframework.transaction.annotation.Transactional
    public String deleteEvent(@PathVariable Long id) {
        Organizer org = (Organizer) session.getAttribute("organizer");
        if (org == null)
            return "redirect:/organizer/login";

        Event event = eventRepo.findById(id).orElseThrow();
        if (!event.getOrganizer().getId().equals(org.getId())) {
            return "redirect:/organizer/events";
        }

        List<Match> matches = matchRepo.findByEventOrderByRoundAscMatchNoAsc(event);
        if (!matches.isEmpty())
            matchRepo.deleteAll(matches);

        List<Entry> entries = entryRepo.findByEventIdOrderByCreatedAtAsc(id);
        if (!entries.isEmpty())
            entryRepo.deleteAll(entries);

        eventRepo.delete(event);
        return "redirect:/organizer/events";
    }

    // --- トーナメント生成 ---
    @PostMapping("/events/{eventId}/generate")
    public String generateTournament(@PathVariable Long eventId) {
        Organizer org = (Organizer) session.getAttribute("organizer");
        if (org == null)
            return "redirect:/organizer/login";

        Event event = eventRepo.findById(eventId).orElseThrow();
        if (!event.getOrganizer().getId().equals(org.getId()))
            return "redirect:/organizer/events";

        tournamentService.generate(event);

        // ★ processByes(event); は削除しました！

        return "redirect:/organizer/events/" + eventId + "/bracket";
    }

    // --- トーナメント表表示 ---
    @GetMapping("/events/{eventId}/bracket")
    public String showBracket(@PathVariable Long eventId, Model model) {
        Organizer org = (Organizer) session.getAttribute("organizer");
        if (org == null)
            return "redirect:/organizer/login";

        Event event = eventRepo.findById(eventId).orElseThrow();
        if (!event.getOrganizer().getId().equals(org.getId()))
            return "redirect:/organizer/events";

        List<Match> matches = matchRepo.findByEventOrderByRoundAscMatchNoAsc(event);
        Map<Integer, List<Match>> roundMap = matches.stream()
                .collect(Collectors.groupingBy(Match::getRound, TreeMap::new, Collectors.toList()));

        model.addAttribute("event", event);
        model.addAttribute("roundMap", roundMap);
        return "organizer/events/bracket";
    }

    // --- 公開設定切り替え ---
    @PostMapping("/events/{eventId}/publish-bracket")
    public String toggleBracketPublish(@PathVariable Long eventId, @RequestParam boolean publish) {
        Organizer org = (Organizer) session.getAttribute("organizer");
        if (org == null)
            return "redirect:/organizer/login";

        Event event = eventRepo.findById(eventId).orElseThrow();
        if (!event.getOrganizer().getId().equals(org.getId()))
            return "redirect:/organizer/events";

        event.setBracketPublic(publish);
        eventRepo.save(event);
        return "redirect:/organizer/events/" + eventId + "/detail";
    }

    // --- 試合結果入力画面の表示 ---
    @GetMapping("/events/{eventId}/matches/{matchId}/edit")
    public String editMatchForm(@PathVariable Long eventId, @PathVariable Long matchId, Model model) {
        Organizer org = (Organizer) session.getAttribute("organizer");
        if (org == null)
            return "redirect:/organizer/login";

        Match match = matchRepo.findById(matchId).orElseThrow();
        model.addAttribute("match", match);
        return "organizer/events/match_edit";
    }

    // --- 試合結果の保存 ＆ 勝ち上がり処理 ---
    @PostMapping("/events/{eventId}/matches/{matchId}/update")
    @org.springframework.transaction.annotation.Transactional
    public String updateMatchResult(
            @PathVariable Long eventId,
            @PathVariable Long matchId,
            @RequestParam("winner") String winner,
            @RequestParam("score") String score) {

        Organizer org = (Organizer) session.getAttribute("organizer");
        if (org == null)
            return "redirect:/organizer/login";

        Match currentMatch = matchRepo.findById(matchId).orElseThrow();
        currentMatch.setWinner(winner);
        currentMatch.setScore(score);
        matchRepo.save(currentMatch);

        // 勝ち上がり処理
        advanceWinner(currentMatch, winner);

        // ★ processByes... は削除しました！

        return "redirect:/organizer/events/" + eventId + "/bracket";
    }

    // 勝者を次の試合に進める
    private void advanceWinner(Match currentMatch, String winner) {
        int nextRound = currentMatch.getRound() + 1;
        int nextMatchNo = (currentMatch.getMatchNo() + 1) / 2;

        Match nextMatch = matchRepo.findByEventAndRoundAndMatchNo(currentMatch.getEvent(), nextRound, nextMatchNo);

        if (nextMatch != null) {
            if (currentMatch.getMatchNo() % 2 != 0) {
                nextMatch.setPlayerA(winner);
            } else {
                nextMatch.setPlayerB(winner);
            }
            matchRepo.save(nextMatch);
        }
    }

    // ★ processByes メソッドは完全に消しました。
}