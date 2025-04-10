package com.noblesse.auth_service.service;

import com.noblesse.auth_service.dto.request.GoogleLoginRequest;
import com.noblesse.auth_service.dto.request.RegisterRequest;
import com.noblesse.auth_service.dto.request.UserAddTopicRequest;
import com.noblesse.auth_service.dto.response.UserResponse;
import com.noblesse.auth_service.entity.Topic;
import com.noblesse.auth_service.entity.User;
import com.noblesse.auth_service.enums.Role;
import com.noblesse.auth_service.exception.AppException;
import com.noblesse.auth_service.exception.ErrorCode;
import com.noblesse.auth_service.repository.TopicRepository;
import com.noblesse.auth_service.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserService {

    UserRepository userRepository;

    TopicRepository topicRepository;

    FriendRequestService friendRequestService;

    private static final String supabaseUrl = "https://eluflzblngwpnjifvwqo.supabase.co/storage/v1/object/images/";
    private static final String supabaseApiKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImVsdWZsemJsbmd3cG5qaWZ2d3FvIiwicm9sZSI6ImFub24iLCJpYXQiOjE3Mjc3OTY3NzMsImV4cCI6MjA0MzM3Mjc3M30.1Xj5Ndd1J6-57JQ4BtEjBTxUqmVNgOhon1BhG1PSz78";

    public UserResponse createUser(RegisterRequest request){

        if (userRepository.existsByUserEmail(request.getUserEmail()))
            throw new AppException(ErrorCode.USER_EXISTED);

        User user = new User();

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setUserEmail(request.getUserEmail());

        HashSet<String> roles = new HashSet<>();
        roles.add(Role.USER.name());

        user.setRoles(roles);
        user.setOnline(false);

        User savedUser = userRepository.save(user);

        String defaultUsername = "User" + user.getUserId();
        user.setUsername(defaultUsername);
        if(request.getAvatar() == null || request.getAvatar().isEmpty()){
            user.setAvatar("https://eluflzblngwpnjifvwqo.supabase.co/storage/v1/object/public/images/avatars/uchiha.jpg");
        }

        savedUser = userRepository.save(savedUser);
        return savedUser.toUserResponse();
    }

    public void setUserOffline(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setOnline(false);
        userRepository.save(user);
        friendRequestService.notifyFriendsAboutOnlineStatus(user);
    }

    public String uploadAvatar(byte[] avatarData,Long userId, String fileName){

//        String extension = fileName.substring(fileName.lastIndexOf("."));
//        String newFileName = UUID.randomUUID().toString() + extension;

        String newFileName = "avatar.jpg";

        String url = supabaseUrl + "avatars/" + userId + "/" + newFileName;
        System.out.println(url);
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.IMAGE_JPEG);
        //headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        headers.set("Authorization", "Bearer " + supabaseApiKey);
        headers.set("x-upsert", "true");

        HttpEntity<byte[]> requestEntity = new HttpEntity<>(avatarData, headers);
        try {
            //restTemplate.exchange(url, HttpMethod.PUT, requestEntity, String.class);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            System.out.println("Response: " + response.getBody());
            return url; // Trả về URL của avatar đã tải lên

        } catch (Exception e) {
            throw new RuntimeException("Failed to upload avatar: " + e.getMessage());
        }
    }

    public UserResponse savedUser(Long userId, String avatarUrl){
        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        user.setAvatar(avatarUrl);
        userRepository.save(user);
        return user.toUserResponse();
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    public User updateUser(Long userId, RegisterRequest request) throws IOException, SQLException {
        if(userRepository.existsByUsername(request.getUsername())) throw  new AppException(ErrorCode.USER_EXISTED);

        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        user.setUsername(request.getUsername());
        //user.setUserEmail(request.getUserEmail());

        return userRepository.save(user);
    }

    public UserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return user.toUserResponse();
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    public List<UserResponse> getAllUsers(){
        log.info("In method get users");

        List<User> users = userRepository.findAll();

        return users.stream().map(User::toUserResponse).collect(Collectors.toList());
    }

    public UserResponse addTopics(Long userId, UserAddTopicRequest request){

        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        List<Topic> selectedTopics = topicRepository.findAllById(request.getTopicsId());
        user.setTopics(selectedTopics);
        user.setStatus("old");
        userRepository.save(user);
        return user.toUserResponse();

    }

    public UserResponse loginWithGoogle(GoogleLoginRequest request) {
        Optional<User> existingUser = userRepository.findByUserEmail(request.getEmail());
        if(existingUser.isPresent() && existingUser.get().getPassword() != null ){
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        User user = existingUser
                .map(existingUserObj -> {
                    existingUserObj.setOnline(true);
                    return userRepository.save(existingUserObj);
                })
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .username(request.getUsername())
                            .userEmail(request.getEmail())
                            .avatar(request.getAvatar())
                            .status("new")
                            .online(true)
                            .build();

                    newUser.setRoles(new HashSet<>());
                    newUser.getRoles().add(Role.USER.name());

                    return userRepository.save(newUser);
                });
        friendRequestService.notifyFriendsAboutOnlineStatus(user);
        return user.toUserResponse();
    }
}
