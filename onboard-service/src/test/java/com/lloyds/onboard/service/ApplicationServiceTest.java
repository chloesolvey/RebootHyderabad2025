package com.lloyds.onboard.service;

import com.lloyds.onboard.Util.EncryptionUtil;
import com.lloyds.onboard.Util.MaskUtil;
import com.lloyds.onboard.entity.Application;
import com.lloyds.onboard.entity.ResumeApplication;
import com.lloyds.onboard.repository.ApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock
    private ApplicationRepository repository;

    @Mock
    private OtpService otpService;

    @InjectMocks
    private ApplicationService service;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(service, "secretKey", "testKey123");
    }
    @Test
    void shouldReturnAllApplications() {
        List<Application> applications = List.of(new Application(), new Application());
        when(repository.findAll()).thenReturn(applications);

        List<Application> result = service.getAllApplications();

        assertEquals(2, result.size());
        verify(repository).findAll();
    }

    @Test
    void shouldReturnApplicationsByRmid() {
        String rmid = "RM001";
        when(repository.findByRmid(rmid)).thenReturn(List.of(new Application()));

        List<Application> result = service.getApplicationsByRmid(rmid);

        assertFalse(result.isEmpty());
        verify(repository).findByRmid(rmid);
    }

    @Test
    void shouldAssignApplicationToRM() {
        Application app = new Application();
        app.setId(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(app));
        when(repository.save(app)).thenReturn(app);

        Application result = service.assignToRM(1L, "RM002");

        assertEquals("RM002", result.getRmid());
        verify(repository).save(app);
    }

    @Test
    void shouldCreateApplication() {
        Application app = new Application();
        when(repository.save(app)).thenReturn(app);

        Application result = service.createApplication(app);

        assertNotNull(result);
        verify(repository).save(app);
    }

    @Test
    void shouldUpdateApplication() {
        Application existing = new Application();
        existing.setId(1L);
        existing.setCreateddate(LocalDateTime.now().minusDays(1));
        existing.setAppid("APP001");

        Application updated = new Application();
        updated.setAppid("APP001");

        when(repository.findByAppid("APP001")).thenReturn(Optional.of(existing));
        when(repository.save(updated)).thenReturn(updated);

        Application result = service.updateApplication("APP001", updated);

        assertEquals(existing.getId(), result.getId());
        assertNotNull(result.getUpdateddate());
    }

    @Test
    void shouldDeleteApplication() {
        service.deleteApplication(1L);
        verify(repository).deleteById(1L);
    }

    @Test
    void shouldGetApplicationById() {
        Application app = new Application();
        when(repository.findById(10L)).thenReturn(Optional.of(app));

        Application result = service.getApplication(10L);

        assertNotNull(result);
        verify(repository).findById(10L);
    }

    @Test
    void shouldReturnResumeApplicationMessage() {
        Application app = new Application();
        app.setAppid("APP123");
        app.setMobilenumber("9876543210");
        app.setId(1L);

        try (MockedStatic<EncryptionUtil> encryptionUtil = Mockito.mockStatic(EncryptionUtil.class);
             MockedStatic<MaskUtil> maskUtil = Mockito.mockStatic(MaskUtil.class)) {

            encryptionUtil.when(() -> EncryptionUtil.decrypt("token123", "testKey123")).thenReturn("APP123");
            when(repository.findByAppid("APP123")).thenReturn(Optional.of(app));
            maskUtil.when(() -> MaskUtil.maskMobileNumber("9876543210")).thenReturn("98******10");

            ResumeApplication result = service.resumeJourney("token123");

            assertEquals("OTP sent to your registered mobile number: 98******10", result.getMessage());
            verify(otpService).generateOtp(any());
        }
    }
}