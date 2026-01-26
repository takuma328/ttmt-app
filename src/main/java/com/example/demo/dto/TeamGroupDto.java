package com.example.demo.dto;

import java.util.ArrayList;
import java.util.List;

import com.example.demo.entity.EntryMember;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TeamGroupDto {
    private String teamName;
    private List<EntryMember> members = new ArrayList<>();

    public TeamGroupDto(String teamName) {
        this.teamName = teamName;
    }

    public void addMember(EntryMember m) {
        members.add(m);
    }

    public String getTeamName() {
        return teamName;
    }

    public List<EntryMember> getMembers() {
        return members;
    }
}
