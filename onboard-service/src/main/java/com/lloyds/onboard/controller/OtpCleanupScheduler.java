package com.lloyds.onboard.controller;

import com.lloyds.onboard.Util.DateTimeUtil;
import com.lloyds.onboard.repository.OtpRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Scheduled component responsible for cleaning up outdated OTP entries from the system.
 * <p>
 * This task runs once daily at midnight and removes OTPs older than 24 hours.
 * </p>
 */
@Component
@Slf4j
public class OtpCleanupScheduler {

    private final OtpRepository otpRepository;

    /**
     * Constructs an instance of {@code OtpCleanupScheduler} with the provided {@link OtpRepository}.
     *
     * @param otpRepository the repository used for OTP deletion
     */
    public OtpCleanupScheduler(OtpRepository otpRepository) {
        this.otpRepository = otpRepository;
    }

    /**
     * Deletes OTP records older than 1 day from the database.
     * <p>
     * This method is scheduled to run daily at midnight using a cron expression.
     * </p>
     * <p>
     * Cron format: {@code 0 0 0 * * *} — meaning at 00:00 every day.
     * </p>
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void deleteOldOtps() {
        log.info("Start delete old Otps");
        LocalDateTime cutofffDate = LocalDateTime.now().minus(1, ChronoUnit.DAYS);
        otpRepository.deleteOtpsOlderThan(DateTimeUtil.format(cutofffDate));
    }
}