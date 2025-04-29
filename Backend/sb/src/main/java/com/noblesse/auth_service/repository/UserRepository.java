package com.noblesse.auth_service.repository;

import com.noblesse.auth_service.dto.request.SearchRequest;
import com.noblesse.auth_service.entity.Post;
import com.noblesse.auth_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUserEmail(String userEmail);

    boolean existsByUserEmail(String userEmail);

    boolean existsByUsername(String username);

    Optional<User> findByUsername(String name);

    @Query("SELECT ut.id FROM User u JOIN u.topics ut WHERE u.userId = :userId")
    List<Long> findUserTopicsId(@Param("userId") Long userId);

    @Query("SELECT u FROM User u WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :#{#request.keyword}, '%'))")
    List<User> findByNameContainingIgnoreCase(@Param("request") SearchRequest request);

}
