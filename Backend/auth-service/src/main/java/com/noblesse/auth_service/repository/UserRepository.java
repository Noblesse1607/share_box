package com.noblesse.auth_service.repository;

import com.noblesse.auth_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUserEmail(String userEmail);

    boolean existsByUserEmail(String userEmail);

}
