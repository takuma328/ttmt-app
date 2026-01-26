package com.example.demo.controller;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.domain.EntryType;
import com.example.demo.domain.EventStatus;
import com.example.demo.entity.Event;
import com.example.demo.service.EventService;

@Controller
@RequestMapping("/admin/events")
@PreAuthorize("hasRole('ADMIN')")
public class AdminEventController {

    private final EventService eventService;

    public AdminEventController(EventService eventService) {
        this.eventService = eventService;
    }

    /** 一覧 */
    @GetMapping
    public String list(Model model) {
        model.addAttribute("events", eventService.findAll());
        return "admin/events/list";
    }

    /** 新規フォーム */
    @GetMapping("/new")
    public String createForm(Model model) {
        Event e = new Event();
        e.setStatus(EventStatus.OPEN);

        model.addAttribute("event", e);
        model.addAttribute("types", EntryType.values());
        return "admin/events/new";
    }

    /** 保存 */
    @PostMapping
    public String create(
            @RequestParam String name,
            @RequestParam EntryType type,
            @RequestParam(required = false) Integer teamSizeMin,
            @RequestParam(required = false) Integer teamSizeMax,
            @RequestParam(required = false) Integer capacity,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime deadline) {
        Event e = new Event();
        e.setName(name);
        e.setType(type);
        e.setTeamSizeMin(teamSizeMin);
        e.setTeamSizeMax(teamSizeMax);
        e.setCapacity(capacity);
        e.setDeadline(deadline);
        e.setStatus(EventStatus.OPEN);

        eventService.save(e);
        return "redirect:/admin/events";
    }

    /** 編集 */
    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("event", eventService.findById(id));
        model.addAttribute("types", EntryType.values());
        return "admin/events/edit";
    }

    /** 更新 */
    @PostMapping("/{id}")
    public String update(
            @PathVariable Long id,
            @RequestParam String name,
            @RequestParam EntryType type,
            @RequestParam(required = false) Integer teamSizeMin,
            @RequestParam(required = false) Integer teamSizeMax,
            @RequestParam(required = false) Integer capacity,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime deadline,
            @RequestParam EventStatus status) {
        Event e = eventService.findById(id);

        e.setName(name);
        e.setType(type);
        e.setTeamSizeMin(teamSizeMin);
        e.setTeamSizeMax(teamSizeMax);
        e.setCapacity(capacity);
        e.setDeadline(deadline);
        e.setStatus(status);

        eventService.save(e);

        return "redirect:/admin/events";
    }

    /** OPEN ↔ CLOSED */
    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable Long id) {
        eventService.toggleOpenClosed(id);
        return "redirect:/admin/events";
    }
}
