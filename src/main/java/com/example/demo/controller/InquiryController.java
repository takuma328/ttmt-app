package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.demo.dto.InquiryForm;
import com.example.demo.service.InquiryService;

@Controller
public class InquiryController {

    @Autowired
    private InquiryService inquiryService;

    // お問い合わせフォーム表示
    @GetMapping("/inquiry")
    public String form(@ModelAttribute("inquiryForm") InquiryForm form) {
        return "inquiry/form";
    }

    // 送信処理
    @PostMapping("/inquiry")
    public String send(@Validated @ModelAttribute("inquiryForm") InquiryForm form,
            BindingResult result) {

        if (result.hasErrors()) {
            return "inquiry/form";
        }

        inquiryService.sendInquiry(form);
        return "redirect:/inquiry/complete";
    }

    // 完了画面
    @GetMapping("/inquiry/complete")
    public String complete() {
        return "inquiry/complete";
    }
}