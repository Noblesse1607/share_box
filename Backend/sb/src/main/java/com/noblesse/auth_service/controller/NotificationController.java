package com.noblesse.auth_service.controller;

import com.noblesse.auth_service.dto.response.ApiResponse;
import com.noblesse.auth_service.dto.response.NotificationResponse;
import com.noblesse.auth_service.service.NotificationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/noti")
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationController {

    NotificationService notificationService;

    @MessageMapping("/friend/request")
    @SendTo("/topic/notifications")
    public String notifyFriendRequest(String message) {
        return message;
    }

    @GetMapping("/receiver/{receiverId}")
    public ApiResponse<List<NotificationResponse>> getNotisByUserId(@PathVariable Long receiverId){
        return ApiResponse.<List<NotificationResponse>>builder()
                .result(notificationService.getNotisByUserId(receiverId))
                .build();
    }

    @PostMapping("/delete/{notiId}")
    public ApiResponse<Void> deleteNotiById(@PathVariable Long notiId) {
        notificationService.deleteNotiById(notiId);
        return ApiResponse.<Void>builder().build();
    }
}
