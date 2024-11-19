package com.jangburich.domain.user.repository;

import java.util.Optional;

import com.jangburich.domain.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByProviderId(String providerId);
}