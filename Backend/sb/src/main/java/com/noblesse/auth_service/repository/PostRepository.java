package com.noblesse.auth_service.repository;

import com.noblesse.auth_service.entity.Post;
import com.noblesse.auth_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    Long countByUser(User user);

    @Query("SELECT p FROM Post p " +
            "WHERE p.user.userId = :userId")
    List<Post> getAllPost(@Param("userId") Long userId);
}
