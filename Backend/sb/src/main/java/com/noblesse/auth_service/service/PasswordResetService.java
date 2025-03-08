package com.noblesse.auth_service.service;

import com.noblesse.auth_service.entity.User;
import com.noblesse.auth_service.entity.VerificationCode;
import com.noblesse.auth_service.repository.UserRepository;
import com.noblesse.auth_service.repository.VerificationCodeRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PasswordResetService {

    UserRepository userRepository;

    VerificationCodeRepository verificationCodeRepository;

    JavaMailSender mailSender;

    PasswordEncoder passwordEncoder;

    private static final int EXPIRATION_MINUTES = 15;

    public boolean generateAndSendVerificationCode(String email) {
        // Find user by email
        Optional<User> userOptional = userRepository.findByUserEmail(email);
        if (userOptional.isEmpty()) {
            return false;
        }

        // Generate a random 6-digit code
        String code = generateRandomCode();

        // Save the code with expiration time
        saveVerificationCode(email, code);

        // Send the code to user's email
        return sendVerificationCodeEmail(email, code);
    }

    /**
     * Verifies if the code is valid for the given email
     */
    public boolean verifyCode(String email, String code) {
        Optional<VerificationCode> verificationOptional = verificationCodeRepository.findByEmailAndCode(email, code);

        if (verificationOptional.isEmpty()) {
            return false;
        }

        VerificationCode verification = verificationOptional.get();

        // Check if code has expired
        if (verification.getExpiryDate().isBefore(LocalDateTime.now())) {
            verificationCodeRepository.delete(verification);
            return false;
        }

        return true;
    }

    /**
     * Resets the user's password using the verification code
     */
    @Transactional
    public boolean resetPassword(String email, String code, String newPassword) {
        // First verify the code
        if (!verifyCode(email, code)) {
            return false;
        }

        // Find the user
        Optional<User> userOptional = userRepository.findByUserEmail(email);
        if (userOptional.isEmpty()) {
            return false;
        }

        User user = userOptional.get();

        // Update the password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Delete the used verification code
        verificationCodeRepository.deleteByEmailAndCode(email, code);

        return true;
    }

    /**
     * Generates a random 6-digit code
     */
    private String generateRandomCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000); // Generates a number between 100000 and 999999
        return String.valueOf(code);
    }

    /**
     * Saves the verification code to the database
     */
    private void saveVerificationCode(String email, String code) {
        // First delete any existing codes for this email
        verificationCodeRepository.deleteByEmail(email);

        // Create new verification code entity
        VerificationCode verificationCode = new VerificationCode();
        verificationCode.setEmail(email);
        verificationCode.setCode(code);
        verificationCode.setExpiryDate(LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES));

        verificationCodeRepository.save(verificationCode);
    }

    /**
     * Sends the verification code to the user's email using MailTrap
     */
    private boolean sendVerificationCodeEmail(String email, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(email);
            helper.setSubject("ShareBox - Password Reset Verification Code");

            String emailContent =
                    "<div style='font-family: Arial, sans-serif; padding: 20px; max-width: 600px; margin: 0 auto;'>" +
                            "<h2 style='color: #333;'>ShareBox Password Reset</h2>" +
                            "<p>You have requested to reset your password. Please use the following verification code:</p>" +
                            "<div style='background-color: #f4f4f4; padding: 15px; text-align: center; font-size: 24px; " +
                            "letter-spacing: 5px; margin: 20px 0; border-radius: 5px;'>" +
                            code +
                            "</div>" +
                            "<p>This code will expire in " + EXPIRATION_MINUTES + " minutes.</p>" +
                            "<p>If you did not request a password reset, please ignore this email.</p>" +
                            "<p>Thank you,<br>ShareBox Team</p>" +
                            "</div>";

            helper.setText(emailContent, true);

            mailSender.send(message);
            return true;
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }

}
