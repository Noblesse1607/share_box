package com.noblesse.auth_service.entity;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.noblesse.auth_service.dto.response.UserResponse;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long userId;

    String username;

    String userEmail;

    String avatar;

    String status;

    String password;

    Set<String> roles;

    Boolean online;

    @ManyToMany
    @JoinTable(
            name = "User_Topic",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "topic_id")
    )
    @JsonManagedReference
    List<Topic> topics;

    @CreationTimestamp
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    LocalDateTime createAt;

    public UserResponse toUserResponse(){
        UserResponse response = new UserResponse();

        response.setUserId(userId);
        response.setUserEmail(userEmail);
        response.setUsername(username);
        response.setRoles(roles);
        response.setUserTopics(topics);
        response.setOnline(online);
        response.setAvatar(avatar);
        response.setStatus(status);
        response.setCreateAt(createAt);

        return response;
    }

}
