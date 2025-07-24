package com.lloyds.onboard.service;

import com.lloyds.onboard.entity.Application;
import com.lloyds.onboard.entity.Otp;
import com.lloyds.onboard.exception.ServiceException;
import com.lloyds.onboard.model.Constants;
import com.lloyds.onboard.model.OtpRequest;
import com.lloyds.onboard.model.OtpVerifyRequest;
import com.lloyds.onboard.repository.ApplicationRepository;
import com.lloyds.onboard.repository.OtpRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Random;

/**
 * Service class for managing OTP (One-Time Password) operations.
 * <p>
 * Provides methods to generate and verify OTPs via SMS or email,
 * with OTP persistence and expiration handling.
 * </p>
 */
@Slf4j
@Service
public class OtpService {

    @Value("${2factor.api.key}")
    private String apiKey;

    @Value("${otp.expiry.minutes:5}")
    private int expiryMinutes;

    private final ApplicationRepository applicationRepository;
    private final OtpRepository otpRepository;
    private final JavaMailSender mailSender;
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Constructs an OtpService with required dependencies.
     *
     * @param applicationRepository the repository to access Application entities
     * @param otpRepository         the repository to access and persist OTP entities
     * @param mailSender            the mail sender used to send OTP emails
     */
    public OtpService(ApplicationRepository applicationRepository, OtpRepository otpRepository, JavaMailSender mailSender) {
        this.applicationRepository = applicationRepository;
        this.otpRepository = otpRepository;
        this.mailSender = mailSender;
    }

    /**
     * Generates and sends an OTP to the recipient based on the mode (SMS or email).
     * The OTP is saved with a timestamp and usage status.
     *
     * @param request the {@link OtpRequest} containing recipient and mode information
     * @return a confirmation message indicating the OTP was sent successfully
     * @throws ServiceException if the mode is invalid or sending the OTP fails
     */
    public String generateOtp(OtpRequest request) throws ServiceException {
        String otp = String.valueOf(100000 + new Random().nextInt(900000));

        if ("sms".equalsIgnoreCase(request.getMode())) {
            sendOtpViaSms(request.getRecipient(), otp);
        } else if ("email".equalsIgnoreCase(request.getMode())) {
            sendOtpViaEmail(request.getRecipient(), otp);
        } else {
            throw new ServiceException(Constants.INVALID_MODE_ERROR);
        }

        Otp entity = new Otp();
        entity.setRecipient(request.getRecipient());
        entity.setOtp(otp);
        entity.setMode(request.getMode());
        entity.setUsed(false);
        entity.setCreatedate(LocalDateTime.now());

        otpRepository.save(entity);

        return "OTP sent successfully via " + request.getMode();
    }

    /**
     * Sends the OTP via SMS using the 2Factor API.
     *
     * @param mobile the recipient mobile number
     * @param otp    the OTP code to send
     * @throws ServiceException if sending the SMS fails
     */
    private void sendOtpViaSms(String mobile, String otp) throws ServiceException {
        String url = "https://2factor.in/API/V1/" + apiKey + "/SMS/" + mobile + "/" + otp;
        log.info("2factor url" + url);
        try {
            restTemplate.getForObject(url, String.class);
        } catch (Exception e) {
            log.error("Error sending OTP via SMS: {}", e.getMessage());
            throw new ServiceException(Constants.SMS_SEND_ERROR);
        }
    }

    /**
     * Sends the OTP via email.
     *
     * @param to  the recipient email address
     * @param otp the OTP code to send
     * @throws ServiceException if sending the email fails
     */
    private void sendOtpViaEmail(String to, String otp) throws ServiceException {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Your OTP Code");
        message.setText("Your OTP is: " + otp);
        try {
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Error sending OTP via Email: {}", e.getMessage());
            throw new ServiceException(Constants.EMAIL_SEND_ERROR);
        }
    }

    /**
     * Verifies the provided OTP for a recipient or application ID.
     * <p>
     * Checks if the OTP exists, is not already used, matches the input, and has not expired.
     * Marks the OTP as used on successful verification.
     * </p>
     *
     * @param request the {@link OtpVerifyRequest} containing OTP and recipient/application ID details
     * @return a message indicating the result of verification
     * @throws ServiceException if the OTP or application is not found, or mobile number is invalid
     */
    public String verifyOtp(OtpVerifyRequest request) throws ServiceException {
        String mobileNumber = null;
        if (request.getId() != null) {
            Application application = applicationRepository.findById(request.getId())
                    .orElseThrow(() -> new ServiceException(Constants.APPLICATION_ID_NOT_FOUND));
            mobileNumber = application.getMobilenumber();
        } else if (request.getRecipient() != null) {
            mobileNumber = request.getRecipient();
        } else {
            throw new ServiceException(Constants.INVALID_MOBILE_NUMBER);
        }

        Otp otp = otpRepository.findTopByRecipientOrderByCreatedateDesc(mobileNumber)
                .orElseThrow(() -> new ServiceException(Constants.OTP_NOT_FOUND));

        if (otp.isUsed()) return "OTP already used.";
        if (!otp.getOtp().equals(request.getOtp())) return "Invalid OTP.";

        LocalDateTime expiry = otp.getCreatedate().plusMinutes(expiryMinutes);
        if (LocalDateTime.now().isAfter(expiry)) return "OTP expired.";

        otp.setUsed(true);
        otpRepository.save(otp);

        return "OTP verified successfully.";
    }
}
