package com.lloyds.onboard.service;

import com.lloyds.onboard.entity.Application;
import com.lloyds.onboard.entity.Otp;
import com.lloyds.onboard.exception.ServiceException;
import com.lloyds.onboard.model.Constants;
import com.lloyds.onboard.model.OtpRequest;
import com.lloyds.onboard.model.OtpVerifyRequest;
import com.lloyds.onboard.repository.ApplicationRepository;
import com.lloyds.onboard.repository.OtpRepository;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OtpServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private OtpRepository otpRepository;

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private OtpService otpService;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(otpService, "apiKey", "dummyApiKey");
        ReflectionTestUtils.setField(otpService, "expiryMinutes", 5);
    }


    @Test
    void shouldThrowIfModeIsInvalid() {
        OtpRequest request = new OtpRequest("fax", "test");

        ServiceException ex = assertThrows(ServiceException.class, () -> otpService.generateOtp(request));
        assertEquals(Constants.INVALID_MODE_ERROR, ex.getErrorCode());
    }

    @Test
    void shouldVerifyOtpSuccessfully() {
        OtpVerifyRequest request = new OtpVerifyRequest();
        request.setRecipient("9999999999");
        request.setOtp("123456");

        Otp otp = new Otp();
        otp.setOtp("123456");
        otp.setUsed(false);
        otp.setCreatedate(LocalDateTime.now().minusMinutes(1));

        when(otpRepository.findTopByRecipientOrderByCreatedateDesc("9999999999")).thenReturn(Optional.of(otp));
        when(otpRepository.save(any())).thenReturn(otp);

        String result = otpService.verifyOtp(request);

        assertEquals("OTP verified successfully.", result);
        verify(otpRepository).save(otp);
    }

    @Test
    void shouldReturnOtpAlreadyUsed() {
        OtpVerifyRequest request = new OtpVerifyRequest("9999999999", "123456", null);

        Otp otp = new Otp();
        otp.setUsed(true);

        when(otpRepository.findTopByRecipientOrderByCreatedateDesc("9999999999")).thenReturn(Optional.of(otp));

        String result = otpService.verifyOtp(request);

        assertEquals("OTP already used.", result);
    }

    @Test
    void shouldReturnInvalidOtp() {
        OtpVerifyRequest request = new OtpVerifyRequest("9999999999", "000000",null);

        Otp otp = new Otp();
        otp.setOtp("123456");
        otp.setUsed(false);

        when(otpRepository.findTopByRecipientOrderByCreatedateDesc("9999999999")).thenReturn(Optional.of(otp));

        String result = otpService.verifyOtp(request);

        assertEquals("Invalid OTP.", result);
    }

    @Test
    void shouldReturnOtpExpired() {
        OtpVerifyRequest request = new OtpVerifyRequest("9999999999", "123456",null);

        Otp otp = new Otp();
        otp.setOtp("123456");
        otp.setUsed(false);
        otp.setCreatedate(LocalDateTime.now().minusMinutes(10));

        when(otpRepository.findTopByRecipientOrderByCreatedateDesc("9999999999")).thenReturn(Optional.of(otp));

        String result = otpService.verifyOtp(request);

        assertEquals("OTP expired.", result);
    }

    @Test
    void shouldGetMobileNumberFromApplicationIfIdPresent() {
        OtpVerifyRequest request = new OtpVerifyRequest();
        request.setOtp("123456");
        request.setId(99L);

        Application app = new Application();
        app.setMobilenumber("8888888888");

        Otp otp = new Otp();
        otp.setOtp("123456");
        otp.setUsed(false);
        otp.setCreatedate(LocalDateTime.now());

        when(applicationRepository.findById(99L)).thenReturn(Optional.of(app));
        when(otpRepository.findTopByRecipientOrderByCreatedateDesc("8888888888")).thenReturn(Optional.of(otp));
        when(otpRepository.save(any())).thenReturn(otp);

        String result = otpService.verifyOtp(request);

        assertEquals("OTP verified successfully.", result);
    }
}