package com.lloyds.onboard.service.notification;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Service implementation for sending SMS notifications.
 * <p>
 * Sends a resume journey link to the user via SMS using the 2Factor API.
 * </p>
 */
@Service
public class SmsService implements NotificationService {

    @Value("${2factor.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Sends an SMS notification containing a resume journey link.
     *
     * @param applicationId the ID of the application to include in the resume link
     * @param mobile        the recipient's mobile number
     * @param firstname     the recipient's first name (currently unused)
     * @param journeytype   the journey type or description (currently unused)
     */
    @Override
    public void sendNotification(String applicationId, String mobile, String firstname, String journeytype) {
        String resumeUrl = "https://example.com/resume-journey/" + applicationId;
        String url = "https://2factor.in/API/V1/" + apiKey + "/SMS/" + mobile + "/" + resumeUrl;

        restTemplate.getForObject(url, String.class);
        // In production, this should be replaced with proper logging and error handling.
    }
}
