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
public class CreatePostRequest {

    String title;
    List<Topic> postTopics;
    String content;
    List<MultipartFile> media;
    Long communityId;


}
