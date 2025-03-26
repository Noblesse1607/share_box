package com.noblesse.auth_service.dto.request;

import com.noblesse.auth_service.enums.MessageType;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MessageMediaRequest {

    MultipartFile content;
    MessageType type;

}
