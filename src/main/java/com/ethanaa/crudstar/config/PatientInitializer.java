package com.ethanaa.crudstar.config;

import com.ethanaa.crudstar.model.persist.patient.FollowUp;
import com.ethanaa.crudstar.model.persist.patient.Medication;
import com.ethanaa.crudstar.model.persist.patient.Patient;
import com.ethanaa.crudstar.repository.PatientPatchEntityRepository;
import com.ethanaa.crudstar.service.PatientService;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

//@Configuration
public class PatientInitializer {

    private static int NUM_PATIENTS = 1_000;

    private PatientService patientService;
    private PatientPatchEntityRepository patientPatchEntityRepository;

    @Autowired
    public PatientInitializer(PatientService patientService, PatientPatchEntityRepository patientPatchEntityRepository) {

        this.patientService = patientService;
        this.patientPatchEntityRepository = patientPatchEntityRepository;
    }

    @PostConstruct
    public void initPatients() {

        patientPatchEntityRepository.deleteAllInBatch();
        patientService.deleteAll();

        List<Patient> patientList = new ArrayList<>();

        for (int i = 0; i < NUM_PATIENTS; i++) {
            Patient patient = new Patient();
            patient.setFirstName(RandomStringUtils.randomAlphabetic(3, 10));
            patient.setLastName(RandomStringUtils.randomAlphabetic(3, 12));
            patient.setDateOfBirth(randomDate(LocalDate.of(1900, 1, 1), LocalDate.now()));
            patient.setMedicalRecordNumber(RandomStringUtils.randomAlphanumeric(5, 15));
            patient.setAddress(RandomStringUtils.randomAlphanumeric(10, 100));
            patient.setPhoneNumber(RandomStringUtils.randomNumeric(10));

            int numMedications = ThreadLocalRandom.current().nextInt(10);
            List<Medication> medications = new ArrayList<>(numMedications);
            for (int j = 0; j < numMedications; j++) {
                Medication medication = new Medication();
                medication.setName(RandomStringUtils.randomAlphabetic(10));
                medication.setDosage(RandomStringUtils.randomNumeric(2) + "mg");
                medication.setStartDate(randomDate(LocalDate.of(1900, 1, 1), LocalDate.now()));
                medication.setEndDate(randomDate(medication.getStartDate(), LocalDate.now()));

                medications.add(medication);
            }

            patient.setMedications(medications);

            int numFollowUps = ThreadLocalRandom.current().nextInt(3);
            List<FollowUp> followUps = new ArrayList<>(numFollowUps);
            for (int j = 0; j < numFollowUps; j++) {
                FollowUp followUp = new FollowUp();
                followUp.setDate(randomDate(LocalDate.now().minusYears(2), LocalDate.now()).atTime(9, 30));
                followUp.setType(randomEnum(FollowUp.Type.class));

                int numNatures = ThreadLocalRandom.current().nextInt(3);
                List<FollowUp.Nature> natures = new ArrayList<>();
                for (int k = 0; k < numNatures; k++) {
                    natures.add(randomEnum(FollowUp.Nature.class));
                }
                followUp.setNatures(natures);

                int numFollowupMedications = ThreadLocalRandom.current().nextInt(3);
                List<Medication> followUpMedications = new ArrayList<>(numFollowupMedications);
                for (int k = 0; k < numMedications; k++) {
                    Medication medication = new Medication();
                    medication.setName(RandomStringUtils.randomAlphabetic(10));
                    medication.setDosage(RandomStringUtils.randomNumeric(2) + "mg");
                    medication.setStartDate(randomDate(LocalDate.of(1900, 1, 1), LocalDate.now()));
                    medication.setEndDate(randomDate(medication.getStartDate(), LocalDate.now()));

                    followUpMedications.add(medication);
                }
                followUp.setMedications(followUpMedications);

                followUps.add(followUp);
            }

            patient.setFollowUps(followUps);

            patientList.add(patient);
        }

        patientService.create(patientList);
    }

    static LocalDate randomDate(LocalDate min, LocalDate max) {

        long minDay = min.toEpochDay();
        long maxDay = max.toEpochDay();
        long randomDay = ThreadLocalRandom.current().nextLong(minDay, maxDay);
        return LocalDate.ofEpochDay(randomDay);
    }

    static <T extends Enum<?>> T randomEnum(Class<T> clazz){

        int x = ThreadLocalRandom.current().nextInt(clazz.getEnumConstants().length);
        return clazz.getEnumConstants()[x];
    }
}
