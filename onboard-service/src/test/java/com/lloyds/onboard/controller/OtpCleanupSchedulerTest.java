package com.lloyds.onboard.controller;

import com.lloyds.onboard.Util.DateTimeUtil;
import com.lloyds.onboard.repository.OtpRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OtpCleanupSchedulerTest {

    @Mock
    private OtpRepository otpRepository;

    private OtpCleanupScheduler scheduler;

    @BeforeEach
    void setup() {
        scheduler = new OtpCleanupScheduler(otpRepository);
    }

    @Test
    void shouldDeleteOtpsOlderThanYesterday() {
        LocalDateTime cutoff = LocalDateTime.now().minus(1, ChronoUnit.DAYS);
        String formattedDate = "2024-06-01T00:00:00"; // Example format

        try (MockedStatic<DateTimeUtil> dateUtilMock = Mockito.mockStatic(DateTimeUtil.class)) {
            dateUtilMock.when(() -> DateTimeUtil.format(any())).thenReturn(formattedDate);

            scheduler.deleteOldOtps();

            dateUtilMock.verify(() -> DateTimeUtil.format(argThat(
                    d -> d.isBefore(LocalDateTime.now()) && d.isAfter(LocalDateTime.now().minusDays(2))
            )));
            verify(otpRepository).deleteOtpsOlderThan(formattedDate);
        }
    }
}