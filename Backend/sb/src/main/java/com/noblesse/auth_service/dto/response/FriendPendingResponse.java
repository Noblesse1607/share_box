package com.noblesse.auth_service.dto.response;

import com.noblesse.auth_service.enums.Status;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FriendPendingResponse {

    Long requestId;
    Long requesterId;
    Status status;

}
