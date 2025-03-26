package com.noblesse.auth_service.dto.response;

import com.noblesse.auth_service.enums.ChatRoomStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatroomResponse {
    Long chatroomId;
    Long user1Id;
    Long user2Id;
    ChatRoomStatus user1Status;
    ChatRoomStatus user2Status;
    String user1_username;
    String user2_username;
    String user1_avatar;
    String user2_avatar;
}
