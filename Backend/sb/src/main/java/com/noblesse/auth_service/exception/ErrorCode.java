package com.noblesse.auth_service.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Uncategorized error", HttpStatus.BAD_REQUEST),
    USER_EXISTED(1002, "User existed", HttpStatus.BAD_REQUEST),
    USERNAME_INVALID(1003, "Username must be at least {min} characters", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1004, "Password must be at least {min} characters", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1005, "User not existed", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1006, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1007, "You do not have permission", HttpStatus.FORBIDDEN),
    USER_NOT_FOUND(1009, "User not found", HttpStatus.UNAUTHORIZED),
    INVALID_DOB(1008, "Your age must be at least {min}", HttpStatus.BAD_REQUEST),
    TOPIC_NOT_FOUND(1011, "Topic not found", HttpStatus.UNAUTHORIZED),
    POST_NOT_FOUND(1012, "Post not found", HttpStatus.NOT_FOUND),
    COMMENT_NOT_FOUND(1014, "Comment not found", HttpStatus.NOT_FOUND),
    COMMUNITY_NOT_FOUND(1015, "Community not found", HttpStatus.NOT_FOUND),
    USER_NOT_MEMBER_OF_COMMUNITY(1016, "User not member in community", HttpStatus.NOT_FOUND),
    USER_ALREADY_MEMBER_OF_COMMUNITY(1017, "User existed in community", HttpStatus.BAD_REQUEST),
    CUSTOMFEED_NOT_FOUND(1015, "Customfeed not found", HttpStatus.NOT_FOUND),
    COMMUNITY_ALREADY_EXISTS_IN_FEED(1016, "Community already exists in feed", HttpStatus.BAD_REQUEST),
    YOU_ARE_NOT_THE_OWNER(1017, "You are not the owner of this post", HttpStatus.UNAUTHORIZED),
    REQUEST_ALREADY_SENT(1018, "Request already sent", HttpStatus.BAD_REQUEST),
    REQUEST_NOT_FOUND(1019, "Request not found", HttpStatus.NOT_FOUND),
    NOT_COMMUNITY_OWNER(1020, "Only community owner can respond to join requests", HttpStatus.UNAUTHORIZED)
    ;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;
}
