package com.jangburich.domain.store.domain;

import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import com.jangburich.domain.menu.domain.MenuResponse;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class StoreGetResponseDTO {
	private Long id;
	private String ownerId;
	private String name;

	@Enumerated(EnumType.STRING)
	private Category category;

	private String representativeImage;
	private Boolean reservationAvailable;
	private Long maxReservation;
	private Long minPrepayment;
	private Long prepaymentDuration;
	private String introduction;
	private Double latitude;
	private Double longitude;
	private String address;
	private String location;
	private String dayOfWeek;
	private String openTime;
	private String closeTime;
	private List<MenuResponse> menuResponses;

	private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

	public StoreGetResponseDTO(Long id, String ownerId, String name, Category category, String representativeImage,
		Boolean reservationAvailable, Long maxReservation, Long minPrepayment, Long prepaymentDuration,
		String introduction,
		Double latitude, Double longitude, String address, String location, String dayOfWeek,
		String openTime,
		String closeTime, List<MenuResponse> menuResponses) {
		this.id = id;
		this.ownerId = ownerId;
		this.name = name;
		this.category = category;
		this.representativeImage = representativeImage;
		this.reservationAvailable = reservationAvailable;
		this.maxReservation = maxReservation;
		this.minPrepayment = minPrepayment;
		this.prepaymentDuration = prepaymentDuration;
		this.introduction = introduction;
		this.latitude = latitude;
		this.longitude = longitude;
		this.address = address;
		this.location = location;
		this.dayOfWeek = dayOfWeek;
		this.openTime = openTime;
		this.closeTime = closeTime;
		this.menuResponses = menuResponses;
	}

	private String convertDayOfWeekToKorean(DayOfWeek dayOfWeek) {
		return switch (dayOfWeek) {
			case MONDAY -> "월";
			case TUESDAY -> "화";
			case WEDNESDAY -> "수";
			case THURSDAY -> "목";
			case FRIDAY -> "금";
			case SATURDAY -> "토";
			case SUNDAY -> "일";
			default -> throw new IllegalArgumentException("Invalid DayOfWeek");
		};
	}

	public StoreGetResponseDTO of(Store store, List<MenuResponse> menuResponses) {
		String dayOfWeekString = store.getWorkDays().stream()
			.map(this::convertDayOfWeekToKorean)
			.collect(Collectors.joining(", "));

		return new StoreGetResponseDTO(
			store.getId(),
			store.getOwner().getUser().getProviderId(),
			store.getName(),
			store.getCategory(),
			store.getRepresentativeImage(),
			store.getReservationAvailable(),
			store.getMaxReservation(),
			store.getMinPrepayment(),
			store.getPrepaymentDuration(),
			store.getIntroduction(),
			store.getLatitude(),
			store.getLongitude(),
			store.getAddress(),
			store.getLocation(),
			dayOfWeekString,
			store.getOpenTime().format(TIME_FORMATTER),
			store.getCloseTime().format(TIME_FORMATTER),
			menuResponses
		);
	}
}
