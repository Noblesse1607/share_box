package com.noblesse.auth_service.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.noblesse.auth_service.enums.MessageType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MessageResponse {

    Long messageId;
    Long chatroomId;
    Long receiverId;
    Long senderId;
    String receiverUsername;
    String senderUsername;
    String receiverAvatar;
    String senderAvatar;
    String content;
    Boolean seen;
    MessageType type;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    LocalDateTime createAt;

}
