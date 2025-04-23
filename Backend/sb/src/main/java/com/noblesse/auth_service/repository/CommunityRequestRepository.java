package com.noblesse.auth_service.repository;

import com.noblesse.auth_service.entity.Community;
import com.noblesse.auth_service.entity.CommunityRequest;
import com.noblesse.auth_service.entity.User;
import com.noblesse.auth_service.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommunityRequestRepository extends JpaRepository<CommunityRequest, Long> {

    List<CommunityRequest> findByRequesterAndStatus(User requester, Status status);

    List<CommunityRequest> findByCommunityAndStatus(Community community, Status status);

    Optional<CommunityRequest> findByRequesterAndCommunityAndStatus(
            User requester, Community community, Status status);

    @Query("SELECT cr FROM CommunityRequest cr WHERE cr.community.id = :communityId AND cr.status = 'PENDING'")
    List<CommunityRequest> findPendingRequestsByCommunityId(@Param("communityId") Long communityId);

    @Query("SELECT cr FROM CommunityRequest cr WHERE cr.community.owner.id = :ownerId AND cr.status = 'PENDING'")
    List<CommunityRequest> findPendingRequestsByOwnerId(@Param("ownerId") Long ownerId);

    Optional<CommunityRequest> findByRequester_UserIdAndCommunity_Id(Long requesterId, Long communityId);

    void deleteAllByCommunityId(Long communityId);

}
