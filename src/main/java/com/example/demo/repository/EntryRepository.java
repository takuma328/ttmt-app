package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Entry;
import com.example.demo.entity.Event;

public interface EntryRepository extends JpaRepository<Entry, Long> {

    List<Entry> findByEventIdOrderByCreatedAtAsc(Long eventId);

    long countByEventId(Long eventId);

    int countByEvent(Event event);

    List<Entry> findByEvent(Event event);

}
