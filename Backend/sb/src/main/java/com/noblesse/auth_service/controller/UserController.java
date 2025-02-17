package com.noblesse.auth_service.controller;

import com.noblesse.auth_service.dto.request.GoogleLoginRequest;
import com.noblesse.auth_service.dto.request.RegisterRequest;
import com.noblesse.auth_service.dto.request.UserAddTopicRequest;
import com.noblesse.auth_service.dto.response.ApiResponse;
import com.noblesse.auth_service.dto.response.UserResponse;
import com.noblesse.auth_service.entity.User;
import com.noblesse.auth_service.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserController {

    UserService userService;

    @PostMapping("/register")
    public ApiResponse<UserResponse> createUser(@RequestBody RegisterRequest request){
        return ApiResponse.<UserResponse>builder()
                .result(userService.createUser(request))
                .build();
    }

    @PostMapping("/google/login")
    public ApiResponse<UserResponse> loginWithGoogle(@ModelAttribute GoogleLoginRequest request){
        return ApiResponse.<UserResponse>builder()
                .result(userService.loginWithGoogle(request))
                .build();
    }

    @DeleteMapping("/delete/{userId}")
    public String deleteUser(@PathVariable Long userId){
        userService.deleteUser(userId);
        return "Delete user success";
    }

    @PostMapping("/{userId}/select-topics")
    public ApiResponse<UserResponse> addTopics(@PathVariable Long userId, @RequestBody UserAddTopicRequest request){
        return ApiResponse.<UserResponse>builder()
                .result(userService.addTopics(userId,request))
                .build();
    }

    @PutMapping("/update/{userId}")
    public ApiResponse<UserResponse> updateUser(@PathVariable Long userId, @ModelAttribute RegisterRequest request) throws IOException, SQLException {
        User saveUser = userService.updateUser(userId, request);
        UserResponse response = new UserResponse();
        response.setUserId(saveUser.getUserId());
        response.setUserEmail(saveUser.getUserEmail());
        response.setUsername(saveUser.getUsername());


        return ApiResponse.<UserResponse>builder()
                .result(response)
                .build();
    }

    @GetMapping("/all")
    public ApiResponse<List<UserResponse>> getAllUsers(){

        var authentication = SecurityContextHolder.getContext().getAuthentication();

        log.info("Username: {}", authentication.getName());
        authentication.getAuthorities().forEach(grantedAuthority -> log.info(grantedAuthority.getAuthority()));

        return ApiResponse.<List<UserResponse>>builder()
                .result(userService.getAllUsers())
                .build();
    }


}
