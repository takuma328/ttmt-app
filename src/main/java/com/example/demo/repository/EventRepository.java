package com.example.demo.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Event;
import com.example.demo.entity.Organizer;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByOrganizer(Organizer organizer);

    List<Event> findByDateGreaterThanEqualOrderByDateAsc(LocalDate date);
}
