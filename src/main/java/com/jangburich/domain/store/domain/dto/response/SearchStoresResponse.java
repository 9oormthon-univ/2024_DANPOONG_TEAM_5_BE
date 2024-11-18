package com.jangburich.domain.store.domain.dto.response;

import com.jangburich.domain.store.domain.Category;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;

@Builder
public record SearchStoresResponse(
        Long storeId,
        String name,
        Boolean isFavorite,
        Category category,
        Double distance,
        String businessStatus,
        String closeTime,
        String phoneNumber,
        String imageUrl
) {

    @QueryProjection
    public SearchStoresResponse(Long storeId, String name, Boolean isFavorite, Category category, Double distance,
                                String businessStatus, String closeTime, String phoneNumber, String imageUrl) {
        this.storeId = storeId;
        this.name = name;
        this.isFavorite = isFavorite;
        this.category = category;
        this.distance = distance;
        this.businessStatus = businessStatus;
        this.closeTime = closeTime;
        this.phoneNumber = phoneNumber;
        this.imageUrl = imageUrl;
    }
}