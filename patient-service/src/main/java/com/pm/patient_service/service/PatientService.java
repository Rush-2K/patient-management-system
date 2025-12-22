package com.pm.patient_service.service;

import com.pm.patient_service.dto.PatientRequestDTO;
import com.pm.patient_service.dto.PatientResponseDTO;
import com.pm.patient_service.exception.EmailAlreadyExistsException;
import com.pm.patient_service.exception.PatientNotFoundException;
import com.pm.patient_service.grpc.BillingServiceGrpcClient;
import com.pm.patient_service.kafka.KafkaProducer;
import com.pm.patient_service.mapper.PatientMapper;
import com.pm.patient_service.model.Patient;
import com.pm.patient_service.projection.top10Response;
import com.pm.patient_service.repository.PatientRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class PatientService {

    private static final Logger log = LogManager.getLogger(PatientService.class);
    private final PatientRepository patientRepository;
    private final BillingServiceGrpcClient billingServiceGrpcClient;
    private final KafkaProducer kafkaProducer;

    public PatientService(PatientRepository patientRepository,
                          BillingServiceGrpcClient billingServiceGrpcClient,
                          KafkaProducer kafkaProducer) {
        this.patientRepository = patientRepository;
        this.billingServiceGrpcClient = billingServiceGrpcClient;
        this.kafkaProducer = kafkaProducer;
    }

    public List<PatientResponseDTO> getPatients () {
        List<Patient> patients = patientRepository.findAll();

        List<PatientResponseDTO> patientResponseDTOS =
                patients.stream().map(patient -> PatientMapper.toDTO(patient)).toList();

        return patientResponseDTOS;
//        return patients.stream().map(PatientMapper::toDTO).toList();
    }

    public PatientResponseDTO createPatient(PatientRequestDTO patientRequestDTO) {

        if(patientRepository.existsByEmail(patientRequestDTO.getEmail())) {
            throw new EmailAlreadyExistsException("A patient with this email "
            + "already exists " +patientRequestDTO.getEmail());
        }

        // save patient in DB
        Patient newPatient = patientRepository.save(
                PatientMapper.toModel(patientRequestDTO));

        // send info to billing service to create billing account
        billingServiceGrpcClient.createBillingAccount(newPatient.getId().toString(),
                newPatient.getName(), newPatient.getEmail());

        // send to kafka
        kafkaProducer.sendEvent(newPatient);
        log.info("Sent Patient Event: [PatientId={}, PatientName={}, " +
                        "PatientEmail={}]", newPatient.getId(),
                newPatient.getName(),
                newPatient.getEmail());

        return PatientMapper.toDTO(newPatient);
    }

    public PatientResponseDTO updatePatient(
            UUID id, PatientRequestDTO patientRequestDTO) {

        Patient patient = patientRepository.findById(id).orElseThrow(
                () -> new PatientNotFoundException("Patient not found with ID: " + id));

        //checks if there is another same email with different id
        if(patientRepository.existsByEmailAndIdNot(patientRequestDTO.getEmail(), id)) {
            throw new EmailAlreadyExistsException("A patient with this email "
                    + "already exists " +patientRequestDTO.getEmail());
        }

        patient.setName(patientRequestDTO.getName());
        patient.setAddress(patientRequestDTO.getAddress());
        patient.setEmail(patientRequestDTO.getEmail());
        patient.setDateOfBirth(LocalDate.parse(patientRequestDTO.getDateOfBirth()));

        Patient updatedPatient = patientRepository.save(patient);

        return PatientMapper.toDTO(updatedPatient);
    }

    public void deletePatient(UUID id) {
        patientRepository.deleteById(id);
    }

    public List<top10Response> getTop10() {
        return patientRepository.findTop10ByOrderByDateOfBirthAsc();
    }

    //for testing
//    @Scheduled(cron = "*/30 * * * * * ")
//    public void notifyPatientToUpdateInfo() {
//        log.info("UPDATING PATIENT DATA...");
//    }

}
