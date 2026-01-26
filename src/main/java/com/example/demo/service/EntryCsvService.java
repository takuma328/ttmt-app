package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.domain.EntryType;
import com.example.demo.entity.Entry;
import com.example.demo.entity.EntryMember;
import com.example.demo.repository.EntryRepository;

@Service
public class EntryCsvService {

    private final EntryRepository entryRepo;

    public EntryCsvService(EntryRepository entryRepo) {
        this.entryRepo = entryRepo;
    }

    public String createCsv(Long eventId) {

        List<Entry> list = entryRepo.findByEventIdOrderByCreatedAtAsc(eventId);

        StringBuilder sb = new StringBuilder();

        sb.append("受付番号,種別,代表者名,メール,電話,チーム名,参加者名,所属,性別,申込日時\n");

        for (Entry e : list) {

            if (e.getType() == EntryType.SINGLE) {

                EntryMember m = e.getMembers().isEmpty() ? null : e.getMembers().get(0);

                sb.append(csv(e.getId())).append(",");
                sb.append(e.getType()).append(",");
                sb.append(csv(e.getContactName())).append(",");
                sb.append(csv(e.getContactEmail())).append(",");
                sb.append(csv(e.getPhone())).append(",");
                sb.append(","); // 団体名なし
                if (m != null) {
                    sb.append(csv(m.getName())).append(",");
                    sb.append(csv(m.getAffiliation())).append(",");
                    sb.append(csv(m.getGender())).append(",");
                } else {
                    sb.append(",,,");
                }
                sb.append(e.getCreatedAt()).append("\n");

            } else {

                for (EntryMember m : e.getMembers()) {

                    sb.append(csv(e.getId())).append(",");
                    sb.append(e.getType()).append(",");
                    sb.append(csv(e.getContactName())).append(",");
                    sb.append(csv(e.getContactEmail())).append(",");
                    sb.append(csv(e.getPhone())).append(",");
                    sb.append(csv(e.getTeamName())).append(",");
                    sb.append(csv(m.getName())).append(",");
                    sb.append(csv(m.getAffiliation())).append(",");
                    sb.append(csv(m.getGender())).append(",");
                    sb.append(e.getCreatedAt()).append("\n");
                }
            }
        }

        return sb.toString();
    }

    private String csv(Object s) {
        if (s == null)
            return "";
        return "\"" + s.toString().replace("\"", "\"\"") + "\"";
    }
}
