package com.noblesse.auth_service.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.noblesse.auth_service.entity.Topic;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
    Long userId;
    String username;
    String userEmail;
    String status;
    Set<String> roles;
    List<Topic> userTopics;
    Boolean online;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    LocalDateTime createAt;
}
