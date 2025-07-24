package com.lloyds.onboard.repository;

import com.lloyds.onboard.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    List<Application> findByRmid(String rmid);
    Optional<Application> findByAppid(String appid);

    List<Application> getApplicationsById(Long id);
}
