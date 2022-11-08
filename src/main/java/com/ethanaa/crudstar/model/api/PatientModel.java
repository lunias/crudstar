package com.ethanaa.crudstar.model.api;

import com.ethanaa.crudstar.model.persist.patient.FollowUp;
import com.ethanaa.crudstar.model.persist.patient.Medication;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDate;
import java.util.List;

public class PatientModel extends RepresentationModel<PatientModel> {

    private String firstName;

    private String lastName;

    private LocalDate dateOfBirth;

    private String medicalRecordNumber;

    private String address;

    private String phoneNumber;

    private List<Medication> medications;

    private List<FollowUp> followUps;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getMedicalRecordNumber() {
        return medicalRecordNumber;
    }

    public void setMedicalRecordNumber(String medicalRecordNumber) {
        this.medicalRecordNumber = medicalRecordNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public List<Medication> getMedications() {
        return medications;
    }

    public void setMedications(List<Medication> medications) {
        this.medications = medications;
    }

    public List<FollowUp> getFollowUps() {
        return followUps;
    }

    public void setFollowUps(List<FollowUp> followUps) {
        this.followUps = followUps;
    }
}
