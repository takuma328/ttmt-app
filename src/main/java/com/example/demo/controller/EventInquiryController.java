package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.demo.dto.InquiryForm;
import com.example.demo.entity.Event;
import com.example.demo.service.EventService;
import com.example.demo.service.InquiryService;

@Controller
public class EventInquiryController {

    @Autowired
    private EventService eventService;

    @Autowired
    private InquiryService inquiryService;

    // フォーム表示
    @GetMapping("/events/{eventId}/inquiry")
    public String form(@PathVariable Long eventId, Model model,
            @ModelAttribute("inquiryForm") InquiryForm form) {
        Event event = eventService.findOne(eventId);
        model.addAttribute("event", event);
        return "public/events/inquiry_form"; // ※このHTMLも作る必要があります
    }

    // 送信処理
    @PostMapping("/events/{eventId}/inquiry")
    public String send(@PathVariable Long eventId,
            @Validated @ModelAttribute("inquiryForm") InquiryForm form,
            BindingResult result, Model model) {
        Event event = eventService.findOne(eventId);

        if (result.hasErrors()) {
            model.addAttribute("event", event);
            return "public/events/inquiry_form";
        }

        // 主催者のメールアドレスへ送信
        String organizerEmail = event.getOrganizer().getEmail();
        inquiryService.sendInquiryToOrganizer(form, organizerEmail, event.getName());

        return "redirect:/events/" + eventId + "/inquiry/complete";
    }

    // 完了画面
    @GetMapping("/events/{eventId}/inquiry/complete")
    public String complete(@PathVariable Long eventId, Model model) {
        model.addAttribute("eventId", eventId);
        return "public/events/inquiry_complete"; // ※このHTMLも作る必要があります
    }
}