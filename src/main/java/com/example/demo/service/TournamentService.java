package com.example.demo.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.domain.EntryType;
import com.example.demo.entity.Entry;
import com.example.demo.entity.EntryMember;
import com.example.demo.entity.Event;
import com.example.demo.entity.Match;
import com.example.demo.repository.EntryMemberRepository;
import com.example.demo.repository.EntryRepository;
import com.example.demo.repository.MatchRepository;

@Service
public class TournamentService {

    @Autowired
    private EntryMemberRepository memberRepo;
    @Autowired
    private EntryRepository entryRepo;
    @Autowired
    private MatchRepository matchRepo;

    @Transactional
    public void generate(Event event) {
        // 既存データをクリア
        List<Match> existingMatches = matchRepo.findByEventOrderByRoundAscMatchNoAsc(event);
        matchRepo.deleteAll(existingMatches);

        // 参加者リスト取得
        List<String> realPlayers;
        if (event.getType() == EntryType.SINGLE) {
            realPlayers = getSingles(event);
        } else {
            realPlayers = getTeams(event);
        }

        if (realPlayers.size() <= 1)
            return;

        // 枠数計算 (4, 8, 16...)
        int powerOfTwo = 2;
        while (powerOfTwo < realPlayers.size()) {
            powerOfTwo *= 2;
        }

        // --- 配置ロジック ---
        Collections.shuffle(realPlayers);

        int byeCount = powerOfTwo - realPlayers.size();
        List<String> bracketList = new ArrayList<>();
        int playersInMatch = realPlayers.size() - byeCount;

        // 試合があるペア
        for (int i = 0; i < playersInMatch; i++) {
            bracketList.add(realPlayers.remove(0));
        }
        // BYEペア
        while (!realPlayers.isEmpty()) {
            bracketList.add(realPlayers.remove(0));
            bracketList.add("BYE");
        }

        // --- Matchデータ作成 ---
        List<Match> allMatches = new ArrayList<>();
        List<Match> round1Matches = new ArrayList<>(); // 1回戦だけ覚えておく

        int currentRound = 1;
        int matchCountInRound = powerOfTwo / 2;

        while (matchCountInRound >= 1) {
            for (int i = 0; i < matchCountInRound; i++) {
                Match m = new Match();
                m.setEvent(event);
                m.setRound(currentRound);
                m.setMatchNo(i + 1);

                allMatches.add(m);

                if (currentRound == 1) {
                    round1Matches.add(m);
                }
            }
            currentRound++;
            matchCountInRound /= 2;
        }

        // 先に全枠を保存してIDを確定させる（超重要）
        matchRepo.saveAll(allMatches);

        // 1回戦に選手を配置 & BYE処理
        int playerIdx = 0;
        for (Match m : round1Matches) {
            String pA = bracketList.get(playerIdx);
            String pB = bracketList.get(playerIdx + 1);
            playerIdx += 2;

            m.setPlayerA(pA);
            m.setPlayerB(pB);

            // BYE判定 & 勝ち上がり処理
            processByeForMatch(m);

            matchRepo.save(m);
        }
    }

    // 1回戦のBYE処理
    private void processByeForMatch(Match m) {
        String winner = null;

        if ("BYE".equals(m.getPlayerA()) && !"BYE".equals(m.getPlayerB())) {
            winner = m.getPlayerB();
        } else if (!"BYE".equals(m.getPlayerA()) && "BYE".equals(m.getPlayerB())) {
            winner = m.getPlayerA();
        }

        if (winner != null) {
            m.setWinner(winner);
            m.setScore("不戦勝");

            // ★ここがポイント：勝者を「次の試合」にだけ進める（再帰しない）
            advanceWinnerToNextRound(m, winner);
        }
    }

    // 勝者を次のラウンドへ送る（1段階だけ）
    private void advanceWinnerToNextRound(Match currentMatch, String winner) {
        int nextRound = currentMatch.getRound() + 1;
        int nextMatchNo = (currentMatch.getMatchNo() + 1) / 2;

        Match nextMatch = matchRepo.findByEventAndRoundAndMatchNo(
                currentMatch.getEvent(), nextRound, nextMatchNo);

        if (nextMatch != null) {
            if (currentMatch.getMatchNo() % 2 != 0) {
                nextMatch.setPlayerA(winner);
            } else {
                nextMatch.setPlayerB(winner);
            }
            matchRepo.save(nextMatch);
        }
    }

    private List<String> getSingles(Event event) {
        return new ArrayList<>(
                memberRepo.findSinglesOrderByEntryId(event)
                        .stream().map(EntryMember::getName).toList());
    }

    private List<String> getTeams(Event event) {
        List<Entry> entries = entryRepo.findByEventIdOrderByCreatedAtAsc(event.getId());
        return new ArrayList<>(
                entries.stream()
                        .map(Entry::getTeamName)
                        .filter(name -> name != null && !name.isBlank())
                        .toList());
    }
}