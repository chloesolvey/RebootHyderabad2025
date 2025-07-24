package com.lloyds.onboard.repository;

import com.lloyds.onboard.entity.RMMappings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RMMappingsRepository extends JpaRepository<RMMappings, Long> {

    // Custom query to get RM ID based on pincode and journey type
    @Query("SELECT r.rmid FROM RMMappings r WHERE r.pincode = :pincode AND r.journeytype = :journeytype")
    String findRmidByPincodeAndJourneytype(String pincode, String journeytype);
}
