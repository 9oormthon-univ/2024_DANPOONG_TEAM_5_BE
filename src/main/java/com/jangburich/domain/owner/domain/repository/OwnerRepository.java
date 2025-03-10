package com.jangburich.domain.owner.domain.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jangburich.domain.owner.domain.Owner;
import com.jangburich.domain.user.domain.User;

public interface OwnerRepository extends JpaRepository<Owner, Long> {
	Optional<Owner> findByUser(User user);
}
