package com.lloyds.onboard.repository;

import com.lloyds.onboard.entity.RmUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RmUserRepository extends JpaRepository<RmUser, Long> {
    Optional<RmUser> findByRmidAndPassword(String rmid, String password);

}
