package com.kiseok.pingmall.common.domain.verification;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface VerificationRepository extends JpaRepository<Verification, Long> {
    Optional<Verification> findByEmail(String email);
}
