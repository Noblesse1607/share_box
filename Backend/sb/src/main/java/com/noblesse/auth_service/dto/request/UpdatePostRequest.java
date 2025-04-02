package com.noblesse.auth_service.dto.request;

import com.noblesse.auth_service.entity.Topic;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdatePostRequest {
    private String title;
    private String content;
    private List<Topic> postTopics;
    private List<MultipartFile> newMedia;
    private List<String> mediaToRemove;
    private Long communityId;
}
