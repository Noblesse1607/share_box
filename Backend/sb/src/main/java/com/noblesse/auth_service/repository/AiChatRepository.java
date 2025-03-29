package com.noblesse.auth_service.repository;

import com.noblesse.auth_service.entity.AiChat;
import jdk.jfr.Registered;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface AiChatRepository extends JpaRepository<AiChat, Long> {
    List<AiChat> findByUserIdAndSessionId(String userId, String sessionId);

    @Transactional
    @Modifying
    @Query("DELETE FROM AiChat a WHERE a.userId = :userId AND a.sessionId = :sessionId")
    void deleteByUserIdAndSessionId(String userId, String sessionId);

    @Query("SELECT COUNT(a) FROM AiChat a WHERE a.userId = :userId AND a.sessionId = :sessionId")
    long countByUserIdAndSessionId(String userId, String sessionId);

    @Query("SELECT a FROM AiChat a WHERE a.userId = :userId AND a.sessionId = :sessionId ORDER BY a.createdAt ASC")
    List<AiChat> findTopNByUserIdAndSessionIdOrderByCreatedAtAsc(
            @Param("userId") String userId,
            @Param("sessionId") String sessionId,
            Pageable pageable);

}
