package com.lloyds.onboard.service;

import com.lloyds.onboard.entity.RmAudit;
import com.lloyds.onboard.repository.RmAuditRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RmAuditServiceTest {

    @Mock
    private RmAuditRepository repo;

    @InjectMocks
    private RmAuditService service;

    @Test
    void shouldCreateAudit() {
        RmAudit audit = new RmAudit();
        audit.setMessage("Created audit entry");

        when(repo.save(audit)).thenReturn(audit);

        RmAudit result = service.createAudit(audit);

        assertNotNull(result);
        assertEquals("Created audit entry", result.getMessage());
        verify(repo).save(audit);
    }

    @Test
    void shouldReturnAuditHistoryByApplicationId() {
        Long applicationId = 101L;
        RmAudit audit1 = new RmAudit();
        RmAudit audit2 = new RmAudit();
        List<RmAudit> history = List.of(audit1, audit2);

        when(repo.findByApplicationid(applicationId)).thenReturn(history);

        List<RmAudit> result = service.getAuditHistory(applicationId);

        assertEquals(2, result.size());
        verify(repo).findByApplicationid(applicationId);
    }
}