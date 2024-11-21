package com.jangburich.domain.team.domain.repository;

import com.jangburich.domain.common.Status;
import com.jangburich.domain.team.domain.Team;
import com.jangburich.domain.team.domain.UserTeam;
import com.jangburich.domain.user.domain.User;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserTeamRepository extends JpaRepository<UserTeam, Long> {
    int countByTeam(Team team);

    boolean existsByUserAndTeam(User user, Team team);

    List<UserTeam> findAllByTeam(Team team);

    List<UserTeam> findAllByTeamAndStatus(Team team, Status status);
}
