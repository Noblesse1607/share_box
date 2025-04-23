package com.noblesse.auth_service.controller;

import com.noblesse.auth_service.dto.response.ApiResponse;
import com.noblesse.auth_service.dto.response.FriendPendingResponse;
import com.noblesse.auth_service.dto.response.UserResponse;
import com.noblesse.auth_service.entity.FriendRequest;
import com.noblesse.auth_service.enums.Status;
import com.noblesse.auth_service.service.FriendRequestService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/friend")
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FriendRequestController {

    FriendRequestService friendRequestService;

    @PostMapping("/request")
    public ResponseEntity<FriendRequest> sendRequest(
            @RequestParam Long requesterId, @RequestParam Long receiverId) {
        FriendRequest friendRequest = friendRequestService.sendFriendRequest(requesterId, receiverId);
        return ResponseEntity.ok(friendRequest);
    }

    @PostMapping("/response")
    public ResponseEntity<ApiResponse<Void>> respondToRequest(
            @RequestParam Long requesterId, @RequestParam Long receiverId, @RequestParam String status) {
        Status requestStatus = Status.valueOf(status.toUpperCase());
        friendRequestService.respondToRequest(requesterId, receiverId, requestStatus);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .message("Friend request updated successfully")
                        .build());
    }

    @PostMapping("/cancel-request")
    public ResponseEntity<ApiResponse<Void>> cancelFriendRequest(
            @RequestParam Long requesterId,
            @RequestParam Long receiverId) {
        friendRequestService.cancelFriendRequest(requesterId, receiverId);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .message("Friend request cancelled successfully")
                        .build());
    }

    @GetMapping("/pending")
    public ApiResponse<List<UserResponse>> getPendingRequests(@RequestParam Long receiverId) {
        return ApiResponse.<List<UserResponse>>builder()
                .result(friendRequestService.getPendingRequests(receiverId))
                .build();
    }

    @GetMapping("/list")
    public ApiResponse<List<UserResponse>> getFriends(@RequestParam Long userId) {
        return ApiResponse.<List<UserResponse>>builder()
                .result(friendRequestService.getFriends(userId))
                .build();
    }

    @GetMapping("/online-list/{userId}")
    public ApiResponse<List<UserResponse>> getOnlineFriends(@PathVariable Long userId) {
        return ApiResponse.<List<UserResponse>>builder()
                .result(friendRequestService.getOnlineFriends(userId))
                .build();
    }

    @DeleteMapping("/{friendId}")
    public ResponseEntity<String> unfriendMethod1(@PathVariable Long friendId,
                                                  @RequestParam Long userId) {
        log.info("Request to unfriend user {} from user {}", friendId, userId);

        friendRequestService.unfriend(userId, friendId);

        return ResponseEntity.ok("Unfriended successfully");
    }

}
