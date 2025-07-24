package com.lloyds.onboard.service;

import com.lloyds.onboard.entity.CustomerFeedback;
import com.lloyds.onboard.repository.CustomerFeedbackRepository;
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
class CustomerFeedbackServiceTest {

    @Mock
    private CustomerFeedbackRepository repository;

    @InjectMocks
    private CustomerFeedbackService service;

    @Test
    void shouldSaveFeedback() {
        CustomerFeedback feedback = new CustomerFeedback();
        feedback.setFeedback("Great experience");

        when(repository.save(feedback)).thenReturn(feedback);

        CustomerFeedback result = service.saveFeedback(feedback);

        assertNotNull(result);
        assertEquals("Great experience", result.getFeedback());
        verify(repository).save(feedback);
    }

    @Test
    void shouldReturnFeedbackByApplicationId() {
        Long applicationId = 10L;
        CustomerFeedback feedback1 = new CustomerFeedback();
        CustomerFeedback feedback2 = new CustomerFeedback();

        List<CustomerFeedback> feedbackList = List.of(feedback1, feedback2);
        when(repository.findByApplicationidOrderByCreateddateDesc(applicationId))
                .thenReturn(feedbackList);

        List<CustomerFeedback> result = service.getFeedbackByApplicationId(applicationId);

        assertEquals(2, result.size());
        verify(repository).findByApplicationidOrderByCreateddateDesc(applicationId);
    }
}