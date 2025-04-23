package com.noblesse.auth_service.repository;

import com.noblesse.auth_service.entity.FriendRequest;
import com.noblesse.auth_service.entity.User;
import com.noblesse.auth_service.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {

    List<FriendRequest> findByReceiverAndStatus(User receiver, Status status);

    List<FriendRequest> findByRequesterAndStatus(User requester, Status status);

    Optional<FriendRequest> findByRequesterUserIdAndReceiverUserId(Long requesterId, Long receiverId);

    Optional<FriendRequest> findByRequesterUserIdAndReceiverUserIdAndStatus(Long requesterId, Long receiverId, Status status);

    @Query("SELECT f FROM FriendRequest f WHERE " +
            "((f.requester.userId = :userId AND f.receiver.userId = :friendId) OR " +
            "(f.requester.userId = :friendId AND f.receiver.userId = :userId)) " +
            "AND f.status = :status")
    Optional<FriendRequest> findByUserIdsAndStatus(
            @Param("userId") Long userId,
            @Param("friendId") Long friendId,
            @Param("status") Status status
    );

    @Query("SELECT f.requester FROM FriendRequest f WHERE f.receiver.id = :userId AND f.status = com.noblesse.auth_service.enums.Status.ACCEPTED " +
            "UNION " +
            "SELECT f.receiver FROM FriendRequest f WHERE f.requester.id = :userId AND f.status = com.noblesse.auth_service.enums.Status.ACCEPTED")
    List<User> findFriendsByUserId(@Param("userId") Long userId);

    @Query("SELECT f.requester FROM FriendRequest f WHERE f.receiver.id = :userId AND f.status = com.noblesse.auth_service.enums.Status.PENDING")
    List<User> findPendingReqsByUserId(@Param("userId") Long userId);

}
