package com.noblesse.auth_service.service;

import com.noblesse.auth_service.dto.response.NotificationResponse;
import com.noblesse.auth_service.entity.Notification;
import com.noblesse.auth_service.enums.NotificationType;
import com.noblesse.auth_service.repository.NotificationRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationService {

    SimpMessagingTemplate messagingTemplate;

    NotificationRepository notificationRepository;

    public void notifyUser(Long userId, String message, String image) {
        Notification notification = new Notification(message, image);
        messagingTemplate.convertAndSend("/topic/user/" + userId, notification);
    }

    public void notifyFriendReq(Long userId, String mes) {
        messagingTemplate.convertAndSend("/topic/friendReq/" + userId, mes);
    }

    public void notifyCommentReply(Long userId, String message, String image, Long commentId, Long postId) {
        Notification notification = Notification.builder()
                .message(message)
                .image(image)
                .receiverId(userId)
                .commentId(commentId)
                .postId(postId)
                .build();

        // Lưu thông báo vào database
        notificationRepository.save(notification);

        // Gửi thông báo realtime
        messagingTemplate.convertAndSend("/topic/comment/reply/" + userId,
                notification.toNotificationResponse());
    }

    // Message
    public void notifyMessage(Long chatroomId, String mes) {
        messagingTemplate.convertAndSend("/topic/message/" + chatroomId, mes);
    }

    public void notifyUnseenMessage(Long chatroomId, Long userId, String mes) {
        messagingTemplate.convertAndSend("/topic/message/unseen/" + chatroomId + "/" + userId, mes);
    }

    public void notifyMessageUpdate(Long chatroomId, Long userId, String mes) {
        messagingTemplate.convertAndSend("/topic/message/change/" + chatroomId + "/" + userId, mes);
    }

    public void notifyMessageSeen(Long chatroomId, Long userId, String mes) {
        messagingTemplate.convertAndSend("/topic/message/seen/" + chatroomId + "/" + userId, mes);
    }

    // Online
    public void notifyOnlineUser(Long userId, String mes) {
        messagingTemplate.convertAndSend("/topic/user/online/" + userId, mes);
    }


    public List<NotificationResponse> getNotisByUserId(Long receiverId){
        List<Notification> notis = notificationRepository.getNotiByUserId(receiverId);
        return notis.stream().map(Notification::toNotificationResponse).collect(Collectors.toList());
    }

    public void deleteNotiById(Long notiId) {
        notificationRepository.deleteById(notiId);
    }

}
