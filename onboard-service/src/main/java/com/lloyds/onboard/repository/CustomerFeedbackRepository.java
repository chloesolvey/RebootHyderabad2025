package com.lloyds.onboard.repository;

import com.lloyds.onboard.entity.CustomerFeedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerFeedbackRepository extends JpaRepository<CustomerFeedback, Long> {
    List<CustomerFeedback> findByApplicationidOrderByCreateddateDesc(Long applicationid);
}
