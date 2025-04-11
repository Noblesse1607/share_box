package com.noblesse.auth_service.repository;

import com.noblesse.auth_service.entity.Favorite;
import com.noblesse.auth_service.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    @Query("SELECT f.post FROM Favorite f WHERE f.user.userId = :userId")
    List<Post> findByUserId(@Param("userId") Long userId);

    @Query("SELECT f FROM Favorite f WHERE f.user.userId = :userId AND f.post.id = :postId")
    Optional<Favorite> findByUserIdAndPostId(@Param("userId") Long userId, @Param("postId") Long postId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Favorite f WHERE f.post.id = :postId")
    void deleteByPostId(@Param("postId") Long postId);

}
