package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Event;
import com.example.demo.entity.Match;

public interface MatchRepository extends JpaRepository<Match, Long> {

    List<Match> findByEventOrderByRoundAscMatchNoAsc(Event event);

    List<Match> findByEventAndRoundOrderByMatchNoAsc(Event event, int round);

    boolean existsByEvent(Event event);

    Match findByEventAndRoundAndMatchNo(Event event, Integer round, Integer matchNo);
}