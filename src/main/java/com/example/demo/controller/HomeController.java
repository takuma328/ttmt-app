package com.example.demo.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.demo.entity.Event; // ★Eventエンティティのインポートが必要です
import com.example.demo.service.EventService;

@Controller
public class HomeController {

    private final EventService eventService;

    public HomeController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping({ "/", "/home" })
    public String home(Model model) {

        // 1. Serviceを使って全件取得
        List<Event> allEvents = eventService.findAll();

        // 2. フィルタリング処理（EventControllerと同じロジック）
        List<Event> futureEvents = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (Event event : allEvents) {
            // 日付が入っていないデータはスキップ
            if (event.getDate() == null)
                continue;

            // 「開催日」が「今日」より前でなければ（＝今日か未来なら）リストに追加
            if (!event.getDate().isBefore(today)) {
                futureEvents.add(event);
            }
        }

        // 3. 日付順に並べ替え（近い順）
        futureEvents.sort((a, b) -> a.getDate().compareTo(b.getDate()));

        // 4. 選別したリストを画面に渡す
        model.addAttribute("events", futureEvents);

        return "home";
    }
}