package com.noblesse.auth_service.service;

import com.noblesse.auth_service.dto.request.UserCreationRequest;
import com.noblesse.auth_service.dto.response.UserResponse;
import com.noblesse.auth_service.entity.User;
import com.noblesse.auth_service.enums.Role;
import com.noblesse.auth_service.exception.AppException;
import com.noblesse.auth_service.exception.ErrorCode;
import com.noblesse.auth_service.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserService {

    UserRepository userRepository;

    public UserResponse createUser(UserCreationRequest request){

        if (userRepository.existsByUserEmail(request.getEmail()))
            throw new AppException(ErrorCode.USER_EXISTED);

        User user = new User();

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setUserEmail(request.getEmail());

        HashSet<String> roles = new HashSet<>();
        roles.add(Role.USER.name());

        user.setRoles(roles);

        userRepository.save(user);

        return user.toUserResponse();
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    public List<UserResponse> getAllUsers(){
        log.info("In method get users");

        List<User> users = userRepository.findAll();

        return users.stream().map(User::toUserResponse).collect(Collectors.toList());
    }
}
