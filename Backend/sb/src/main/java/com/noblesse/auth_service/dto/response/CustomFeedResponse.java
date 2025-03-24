package com.noblesse.auth_service.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.noblesse.auth_service.entity.Community;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CustomFeedResponse {
    Long customfeedId;

    String name;

    String description;

    Long ownerId;

    List<Community> communities;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    LocalDateTime createAt;
}
