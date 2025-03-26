package com.noblesse.auth_service.dto.request;

import com.noblesse.auth_service.enums.MessageType;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MessageRequest {
    String content;
    MessageType type;
}
