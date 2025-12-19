package com.pm.patient_service.repository;

import com.pm.patient_service.model.Patient;
import com.pm.patient_service.projection.top10Response;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PatientRepository extends JpaRepository<Patient, UUID> {

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, UUID id);

//    @Query("SELECT p.name AS name, p.email AS email, p.dateOfBirth AS dateOfBirth FROM Patient p ORDER BY p.dateOfBirth ASC")
    List<top10Response> findTop10ByOrderByDateOfBirthAsc();

//    List<top10Response> findTop10test2();
}
