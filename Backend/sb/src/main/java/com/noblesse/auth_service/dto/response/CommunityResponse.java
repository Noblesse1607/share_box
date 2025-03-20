package com.noblesse.auth_service.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.noblesse.auth_service.entity.User;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommunityResponse {
    Long communityId;

    String name;

    String description;

    String avatar;

    String backgroundImg;

    Long ownerId;

    List<User> members;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    LocalDateTime createAt;
}
