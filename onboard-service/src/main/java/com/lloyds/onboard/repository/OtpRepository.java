package com.lloyds.onboard.repository;

import com.lloyds.onboard.entity.Otp;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Transactional
@Repository
public interface OtpRepository extends JpaRepository<Otp, Long> {
    Optional<Otp> findTopByRecipientOrderByCreatedateDesc(String recipient);

    @Modifying
    @Query(value = "DELETE FROM otp WHERE createdate < :expiryTime", nativeQuery = true)
    void deleteOtpsOlderThan(@Param("expiryTime") String expiryTime);
}
