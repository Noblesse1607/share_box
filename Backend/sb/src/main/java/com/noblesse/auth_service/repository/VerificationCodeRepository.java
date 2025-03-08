package com.noblesse.auth_service.repository;

import com.noblesse.auth_service.entity.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VerificationCodeRepository extends JpaRepository<VerificationCode,Long> {

    Optional<VerificationCode> findByEmailAndCode(String email, String code);

    void deleteByEmail(String email);

    void deleteByEmailAndCode(String email, String code);

}
