package com.noblesse.auth_service.controller;

import com.noblesse.auth_service.dto.request.EmailRequest;
import com.noblesse.auth_service.dto.request.ResetPasswordRequest;
import com.noblesse.auth_service.dto.response.ApiResponse;
import com.noblesse.auth_service.dto.response.VerificationCodeResponse;
import com.noblesse.auth_service.service.PasswordResetService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/forgot-password")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PasswordResetController {

    private PasswordResetService passwordResetService;

    @PostMapping("/request")
    public ResponseEntity<ApiResponse<Void>> requestVerificationCode(@RequestBody EmailRequest emailRequest) {
        boolean success = passwordResetService.generateAndSendVerificationCode(emailRequest.getEmail());
        if (success) {
            return ResponseEntity.ok(ApiResponse.<Void>builder()
                    .code(1000)
                    .message("Verification code sent successfully")
                    .build());
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.<Void>builder()
                    .code(1001)
                    .message("Email not found or error sending code")
                    .build());
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<Void>> verifyCode(@RequestBody VerificationCodeResponse verificationRequest) {
        boolean isValid = passwordResetService.verifyCode(
                verificationRequest.getEmail(),
                verificationRequest.getVerificationCode()
        );

        if (isValid) {
            return ResponseEntity.ok(ApiResponse.<Void>builder()
                    .code(1000)
                    .message("Verification code is valid")
                    .build());
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.<Void>builder()
                    .code(1001)
                    .message("Invalid verification code")
                    .build());
        }
    }

    @PostMapping("/reset")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@RequestBody ResetPasswordRequest resetRequest) {
        boolean success = passwordResetService.resetPassword(
                resetRequest.getEmail(),
                resetRequest.getVerificationCode(),
                resetRequest.getNewPassword()
        );

        if (success) {
            return ResponseEntity.ok(ApiResponse.<Void>builder()
                    .code(1000)
                    .message("Password reset successful")
                    .build());
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.<Void>builder()
                    .code(1001)
                    .message("Invalid request or code expired")
                    .build());
        }
    }

}
