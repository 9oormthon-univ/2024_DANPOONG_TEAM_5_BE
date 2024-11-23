package com.jangburich.domain.store.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class StoreTeamResponse {
    Long id;
    Integer remainPoint;
    Long teamId;
    String teamName;
    String teamDescription;
    Long storeId;
    String period;

    public StoreTeamResponse(Long id, Integer remainPoint, Long teamId, String teamName, String teamDescription,
                             Long storeId, LocalDateTime period, long maxReservation) {
        this.id = id;
        this.remainPoint = remainPoint;
        this.teamId = teamId;
        this.teamName = teamName;
        this.teamDescription = teamDescription;
        this.storeId = storeId;
        this.period = calculateDateRange(period, maxReservation);
    }

    private String calculateDateRange(LocalDateTime updatedAt, long maxReservation) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
        // 시작 날짜
        String startDate = updatedAt.format(formatter);
        // 종료 날짜
        LocalDateTime endDateTime = updatedAt.plusDays(maxReservation);
        String endDate = endDateTime.format(formatter);
        // 범위 문자열 생성
        return startDate + " ~ " + endDate;
    }
}
