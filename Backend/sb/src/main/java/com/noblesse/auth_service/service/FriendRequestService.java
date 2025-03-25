package com.noblesse.auth_service.service;

import com.noblesse.auth_service.dto.response.FriendPendingResponse;
import com.noblesse.auth_service.dto.response.UserResponse;
import com.noblesse.auth_service.entity.FriendRequest;
import com.noblesse.auth_service.entity.User;
import com.noblesse.auth_service.enums.Status;
import com.noblesse.auth_service.repository.FriendRequestRepository;
import com.noblesse.auth_service.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FriendRequestService {

    FriendRequestRepository friendRequestRepository;

    UserRepository userRepository;

    NotificationService notificationService;

    public FriendRequest sendFriendRequest(Long requesterId, Long receiverId) {
        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new RuntimeException("Requester not found"));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        if (friendRequestRepository.findByRequesterAndStatus(requester, Status.PENDING)
                .stream()
                .anyMatch(request -> request.getReceiver().equals(receiver))) {
            throw new RuntimeException("Friend request already sent");
        }

        FriendRequest friendRequest = FriendRequest.builder()
                .requester(requester)
                .receiver(receiver)
                .status(Status.PENDING)
                .build();

        notificationService.notifyUser(receiver.getUserId(),
                "You have a new friend request from " + requester.getUsername());

        return friendRequestRepository.save(friendRequest);
    }

    public FriendRequest respondToRequest(Long requesterId, Long receiverId, Status status) {
        FriendRequest friendRequest = friendRequestRepository.findByRequesterUserIdAndReceiverUserId(requesterId, receiverId)
                .orElseThrow(() -> new RuntimeException("Friend Request not found"));

        if (!friendRequest.getReceiver().getUserId().equals(receiverId)) {
            throw new RuntimeException("Only the receiver can respond to the friend request");
        }

        friendRequest.setStatus(status);

        String message = status == Status.ACCEPTED
                ? "Your friend request was accepted!"
                : "Your friend request was rejected.";
        notificationService.notifyUser(friendRequest.getRequester().getUserId(), message);

        return friendRequestRepository.save(friendRequest);
    }

    public List<FriendPendingResponse> getPendingRequests(Long receiverId) {
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        List<FriendRequest> friendRequests = friendRequestRepository.findByReceiverAndStatus(receiver, Status.PENDING);

        return friendRequests.stream().map(FriendRequest::toFriendPendingResponse).collect(Collectors.toList());
    }

    public List<UserResponse> getFriends(Long userId) {
        List<User> users = friendRequestRepository.findFriendsByUserId(userId);
        return users.stream().map(User::toUserResponse).collect(Collectors.toList());
    }

}
