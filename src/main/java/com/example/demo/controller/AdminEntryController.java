// src/main/java/com/example/demo/controller/AdminEntryController.java
package com.example.demo.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.demo.entity.Entry;
import com.example.demo.entity.Event;
import com.example.demo.repository.EntryRepository;
import com.example.demo.repository.EventRepository;
import com.example.demo.service.EntryCsvService;

@Controller
@RequestMapping("/admin/entries")
public class AdminEntryController {

    private final EventRepository eventRepo;
    private final EntryRepository entryRepo;
    private final EntryCsvService csvService;

    public AdminEntryController(EventRepository eventRepo,
            EntryRepository entryRepo,
            EntryCsvService csvService) {
        this.eventRepo = eventRepo;
        this.entryRepo = entryRepo;
        this.csvService = csvService;
    }

    /** 一覧 */
    @GetMapping
    public String list(@RequestParam Long eventId, Model model) {

        Event event = eventRepo.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        List<Entry> entries = entryRepo.findByEventIdOrderByCreatedAtAsc(eventId);

        model.addAttribute("event", event);
        model.addAttribute("entries", entries);

        return "admin/entries/list";
    }

    /** 詳細 */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {

        Entry entry = entryRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Entry not found"));

        model.addAttribute("entry", entry);
        return "admin/entries/detail";
    }

    /** CSV出力 */
    @GetMapping("/csv")
    @ResponseBody
    public String csv(@RequestParam Long eventId) {

        Event event = eventRepo.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        return csvService.createCsv(eventId);
    }
}
