package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.domain.EventStatus;
import com.example.demo.entity.Event;
import com.example.demo.entity.Organizer;
import com.example.demo.repository.EventRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class EventService {

    private final EventRepository eventRepo;

    public EventService(EventRepository eventRepo) {
        this.eventRepo = eventRepo;
    }

    /** 全件取得（管理者用） */
    public List<Event> findAll() {
        return eventRepo.findAll();
    }

    /** 主催者ごとの大会取得 */
    public List<Event> findByOrganizer(Organizer org) {
        return eventRepo.findByOrganizer(org);
    }

    /** ID で取得 (findById) */
    public Event findById(Long id) {
        return eventRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Event not found: " + id));
    }

    /** * ID で取得 (findOne) 
     * ※コントローラーから呼ばれているメソッド名に合わせました
     */
    public Event findOne(Long id) {
        return findById(id); // 中身はfindByIdと同じ動きでOK
    }

    /** 保存（新規 or 更新） */
    public Event save(Event e) {
        return eventRepo.save(e);
    }

    /** Organizer 用：新規作成 */
    public Event create(Event e) {
        return eventRepo.save(e);
    }

    /** OPEN ↔ CLOSED */
    public void toggleOpenClosed(Long id) {
        Event e = findById(id);
        if (e.getStatus() == EventStatus.OPEN) {
            e.setStatus(EventStatus.CLOSED);
        } else {
            e.setStatus(EventStatus.OPEN);
        }
        eventRepo.save(e);
    }
}