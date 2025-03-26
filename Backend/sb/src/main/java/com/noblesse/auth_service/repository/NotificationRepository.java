package com.noblesse.auth_service.repository;

import com.noblesse.auth_service.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("SELECT n FROM Notification n WHERE n.receiverId = :receiverId")
    List<Notification> getNotiByUserId(@Param("receiverId") Long receiverId);

}
