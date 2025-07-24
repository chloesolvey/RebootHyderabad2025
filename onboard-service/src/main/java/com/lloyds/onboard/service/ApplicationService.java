package com.lloyds.onboard.service;

import com.lloyds.onboard.Util.EncryptionUtil;
import com.lloyds.onboard.Util.MaskUtil;
import com.lloyds.onboard.entity.Application;
import com.lloyds.onboard.entity.ResumeApplication;
import com.lloyds.onboard.exception.ServiceException;
import com.lloyds.onboard.model.Constants;
import com.lloyds.onboard.model.OtpRequest;
import com.lloyds.onboard.repository.ApplicationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service class for managing Application entities.
 * <p>
 * Provides methods to create, retrieve, update, delete, and assign applications,
 * as well as resuming an application journey using a secure token.
 * </p>
 */
@Service
@Slf4j
public class ApplicationService {

    private final ApplicationRepository repo;
    private final OtpService otpService;

    @Value("${encryption.key}")
    private String secretKey;

    /**
     * Constructs an ApplicationService with the specified repository and OTP service.
     *
     * @param repo       the repository for accessing Application entities
     * @param otpService the service for generating OTPs
     */
    public ApplicationService(ApplicationRepository repo, OtpService otpService) {
        this.repo = repo;
        this.otpService = otpService;
    }

    /**
     * Retrieves all applications.
     *
     * @return a list of all {@link Application} entities
     */
    public List<Application> getAllApplications() {
        return repo.findAll();
    }

    /**
     * Retrieves applications assigned to a specific RM ID.
     *
     * @param rmid the RM ID to filter applications by
     * @return a list of {@link Application} entities assigned to the given RM ID
     */
    public List<Application> getApplicationsByRmid(String rmid) {
        return repo.findByRmid(rmid);
    }

    /**
     * Assigns an application to a Relationship Manager (RM) by updating the RM ID.
     *
     * @param id   the ID of the application to assign
     * @param rmid the RM ID to assign the application to
     * @return the updated {@link Application} entity
     * @throws ServiceException if the application ID is not found
     */
    public Application assignToRM(Long id, String rmid) {
        Application app = repo.findById(id)
                .orElseThrow(() -> new ServiceException(Constants.APPLICATION_ID_NOT_FOUND));
        app.setRmid(rmid);
        return repo.save(app);
    }

    /**
     * Creates a new application.
     *
     * @param app the {@link Application} entity to create
     * @return the created {@link Application} entity
     */
    public Application createApplication(Application app) {
        return repo.save(app);
    }

    /**
     * Updates an existing application identified by application ID.
     * <p>
     * Preserves the original creation date and sets the updated date to now.
     * </p>
     *
     * @param appid   the application ID to update
     * @param updated the updated {@link Application} data
     * @return the updated {@link Application} entity
     * @throws ServiceException if the application ID is not found
     */
    public Application updateApplication(String appid, Application updated) {
        Application existing = repo.findByAppid(appid)
                .orElseThrow(() -> new ServiceException(Constants.APPLICATION_ID_NOT_FOUND));
        updated.setId(existing.getId());
        updated.setCreateddate(existing.getCreateddate());
        updated.setUpdateddate(LocalDateTime.now());
        return repo.save(updated);
    }

    /**
     * Deletes an application by its ID.
     *
     * @param id the ID of the application to delete
     */
    public void deleteApplication(Long id) {
        repo.deleteById(id);
    }

    /**
     * Retrieves an application by its ID.
     *
     * @param applicationId the ID of the application to retrieve
     * @return the {@link Application} entity if found
     * @throws ServiceException if the application ID is not found
     */
    public Application getApplication(Long applicationId) {
        return repo.findById(applicationId)
                .orElseThrow(() -> new ServiceException(Constants.APPLICATION_ID_NOT_FOUND));
    }

    /**
     * Resumes an application journey using an encrypted token.
     * <p>
     * Decrypts the token to get the application ID, fetches the application,
     * generates an OTP sent to the registered mobile number, and returns a
     * {@link ResumeApplication} containing a masked mobile number message.
     * </p>
     *
     * @param token the encrypted token representing the application ID
     * @return a {@link ResumeApplication} with OTP sent message and application ID
     * @throws ServiceException if decryption fails or application is not found
     */
    public ResumeApplication resumeJourney(String token) throws ServiceException {
        String applicationId;
        try {
            applicationId = EncryptionUtil.decrypt(token, secretKey);
        } catch (Exception e) {
            log.error("Error decrypting applicationId", e);
            throw new ServiceException(Constants.DECRYPTION_ERROR);
        }
        Application app = repo.findByAppid(applicationId)
                .orElseThrow(() -> new ServiceException(Constants.APPLICATION_ID_NOT_FOUND));

        OtpRequest otpRequest = new OtpRequest();
        otpRequest.setMode("sms");
        otpRequest.setRecipient(app.getMobilenumber());
        otpService.generateOtp(otpRequest);

        ResumeApplication resumeApp = new ResumeApplication();
        resumeApp.setId(app.getId());
        resumeApp.setMessage("OTP sent to your registered mobile number: " + MaskUtil.maskMobileNumber(app.getMobilenumber()));
        return resumeApp;
    }
}
