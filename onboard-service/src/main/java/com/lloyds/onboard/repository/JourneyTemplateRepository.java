// src/main/java/com/example/demo/repository/JourneyTemplateRepository.java
package com.lloyds.onboard.repository;

import com.lloyds.onboard.entity.JourneyTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JourneyTemplateRepository extends JpaRepository<JourneyTemplate, Long> {
}
