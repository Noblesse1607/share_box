package com.noblesse.auth_service.entity;


import com.noblesse.auth_service.dto.response.UserResponse;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;
import lombok.experimental.FieldDefaults;

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
    Long id;

    String username;

    String userEmail;

    String avatar;

    String password;

    Set<String> roles;

    public UserResponse toUserResponse(){
        UserResponse response = new UserResponse();

        response.setId(id);
        response.setUserEmail(userEmail);
        response.setRoles(roles);

        return response;
    }

}
