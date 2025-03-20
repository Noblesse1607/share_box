package com.noblesse.auth_service.repository;

import com.noblesse.auth_service.entity.CustomFeed;
import com.noblesse.auth_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomFeedRepository extends JpaRepository<CustomFeed,Long> {
    List<CustomFeed> findByOwner(User user);
}
