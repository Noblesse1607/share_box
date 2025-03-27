package com.noblesse.auth_service.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.noblesse.auth_service.dto.response.NotificationResponse;
import com.noblesse.auth_service.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String message;

    String image;
    Long receiverId;

    @CreationTimestamp
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    LocalDateTime createAt;

    Long commentId;

    Long postId;


    public Notification(String message, String image) {
        this.message = message;
        this.image = image;
    }

    public NotificationResponse toNotificationResponse() {
        NotificationResponse notificationResponse = new NotificationResponse();
        notificationResponse.setNotiId(id);
        notificationResponse.setMessage(message);
        notificationResponse.setImage(image);
        notificationResponse.setReceiverId(receiverId);
        notificationResponse.setCreateAt(createAt);
        notificationResponse.setCommentId(commentId);
        notificationResponse.setPostId(postId);

        return notificationResponse;
    }

}
