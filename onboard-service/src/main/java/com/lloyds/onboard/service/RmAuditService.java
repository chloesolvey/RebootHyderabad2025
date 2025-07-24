package com.lloyds.onboard.service;

import com.lloyds.onboard.entity.RmAudit;
import com.lloyds.onboard.repository.RmAuditRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service class for managing RM (Relationship Manager) audit records.
 * <p>
 * Provides methods to create audit entries and retrieve audit history by application ID.
 * </p>
 */
@Service
public class RmAuditService {

  private final RmAuditRepository repo;

  /**
   * Constructs an RmAuditService with the specified repository.
   *
   * @param repo the repository used for CRUD operations on RmAudit entities
   */
  public RmAuditService(RmAuditRepository repo) {
    this.repo = repo;
  }

  /**
   * Creates and saves a new RM audit record.
   *
   * @param audit the {@link RmAudit} entity to save
   * @return the saved {@link RmAudit} entity
   */
  public RmAudit createAudit(RmAudit audit) {
    return repo.save(audit);
  }

  /**
   * Retrieves a list of RM audit records associated with the specified application ID.
   *
   * @param applicationId the ID of the application whose audit history is requested
   * @return a list of {@link RmAudit} entities related to the given application ID
   */
  public List<RmAudit> getAuditHistory(Long applicationId) {
    return repo.findByApplicationid(applicationId);
  }

}
