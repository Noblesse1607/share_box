package com.noblesse.auth_service.controller;

import com.noblesse.auth_service.dto.response.ApiResponse;
import com.noblesse.auth_service.dto.response.ChatroomResponse;
import com.noblesse.auth_service.enums.ChatRoomStatus;
import com.noblesse.auth_service.service.ChatroomService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chatroom")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ChatroomController {
    ChatroomService chatroomService;

    @PostMapping("/change-status")
    public ApiResponse<Void> setUserStatus(@RequestParam Long chatroomId, @RequestParam Long userId, @RequestParam String status) {
        ChatRoomStatus requestStatus = ChatRoomStatus.valueOf(status.toUpperCase());
        chatroomService.setUserStatus(chatroomId, userId, requestStatus);
        return ApiResponse.<Void>builder()
                .message("Status updated successfully")
                .build();
    }

    @GetMapping("/get/{user1Id}/{user2Id}")
    public ApiResponse<ChatroomResponse> getChatroomByUserId(@PathVariable Long user1Id, @PathVariable Long user2Id) {
        return ApiResponse.<ChatroomResponse>builder()
                .result(chatroomService.getChatroomByUserId(user1Id, user2Id))
                .build();
    }

    @GetMapping("/getAll/{userId}")
    public ApiResponse<List<ChatroomResponse>> getAllByUserId(@PathVariable Long userId) {
        return ApiResponse.<List<ChatroomResponse>>builder()
                .result(chatroomService.getAllByUserId(userId))
                .build();
    }
}
