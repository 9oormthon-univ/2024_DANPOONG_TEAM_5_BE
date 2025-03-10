package com.jangburich.domain.team.domain;

import java.util.UUID;

import com.jangburich.domain.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Team extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", updatable = false)
	private Long id;

	@Column(name = "name")
	private String name;

	@Column(name = "description")
	private String description;

	@Column(name = "secret_code", updatable = false, unique = true)
	private String secretCode;

	@Embedded
	private TeamLeader teamLeader;

	@Column(name = "point")
	private Integer point;

	@Enumerated(EnumType.STRING)
	@Column(name = "team_type")
	private TeamType teamType;

	public void updatePoint(Integer point) {
		this.point += point;
	}

    public void validateJoinCode(String joinCode) {
        if (!this.secretCode.equals(joinCode)) {
            throw new IllegalArgumentException("유효하지 않은 입장 코드입니다.");
        }
    }

	@PrePersist
	private void generateSecretCode() {
		if (this.secretCode == null) {
			this.secretCode = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
		}
	}

	@Builder
	public Team(String name, String description, TeamLeader teamLeader, Integer point,
		TeamType teamType) {
		this.name = name;
		this.description = description;
		this.teamLeader = teamLeader;
		this.point = point;
		this.teamType = teamType;
	}

	public void validateIsTeamLeader(Long userId, Long userId1) {
		if (!userId.equals(userId1)) {
			throw new IllegalArgumentException("팀의 리더가 아닌 사람은 선결제를 할 수 없습니다.");
		}
	}
}
