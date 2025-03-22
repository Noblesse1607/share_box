package com.noblesse.auth_service.repository;

import com.noblesse.auth_service.dto.request.SearchRequest;
import com.noblesse.auth_service.entity.Community;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommunityRepository extends JpaRepository<Community,Long> {
    @Query("SELECT c FROM Community c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :#{#request.keyword}, '%'))")
    List<Community> findByNameContainingIgnoreCase(@Param("request") SearchRequest request);

    @Query("SELECT c FROM Community c JOIN c.members m WHERE m.userId = :userId")
    List<Community> findUserCommunities(@Param("userId") Long userId);
}
