package edu.ncsu.csc.iTrust2.services;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import edu.ncsu.csc.iTrust2.models.PatientAdvocateAssignment;
import edu.ncsu.csc.iTrust2.models.User;
import edu.ncsu.csc.iTrust2.models.enums.Role;
import edu.ncsu.csc.iTrust2.repositories.PatientAdvocateAssignmentRepository;

/**
 * This service will facilitate creating, deleting, and retrieving patient
 * advocate assignments
 *
 * @author ropeace
 *
 */
@Component
@Transactional
public class PatientAdvocateAssignmentService extends Service<PatientAdvocateAssignment, Long> {

    /**
     * The PatientAdvocateAssignmentRepository used to perform CRUD operations
     */
    @Autowired
    private PatientAdvocateAssignmentRepository repository;

    @Override
    protected JpaRepository<PatientAdvocateAssignment, Long> getRepository () {
        return this.repository;
    }

    /**
     * Retrieves assignments for a patient
     *
     * @param patient
     *            the patient to get assignments with
     * @return the list of assignments for that patient
     */
    public List<PatientAdvocateAssignment> getAssignmentsByPatient ( final User patient ) {
        if ( !patient.getRoles().contains( Role.ROLE_PATIENT ) ) {
            throw new IllegalArgumentException( "The patient to search with must have the patient role" );
        }
        return repository.findByPatient( patient );
    }

    /**
     * Retrieves assignments for a patient advocate
     *
     * @param patientAdvocate
     *            the patient advocate to get assignments with
     * @return the list of assignments for that advocate
     */
    public List<PatientAdvocateAssignment> getAssignmentsByPatientAdvocate ( final User patientAdvocate ) {
        if ( !patientAdvocate.getRoles().contains( Role.ROLE_ADVOCATE ) ) {
            throw new IllegalArgumentException(
                    "The patient advocate to search with must have the patient advocate role" );
        }
        return repository.findByPatientAdvocate( patientAdvocate );
    }

    /**
     * Retrieves an assignment based on both the patient and advocate if it
     * exists
     *
     * @param patient
     *            the patient in the assignment
     * @param patientAdvocate
     *            the advocate in the assignment
     * @return the assignment
     */
    public PatientAdvocateAssignment getAssignment ( final User patient, final User patientAdvocate ) {
        if ( !patient.getRoles().contains( Role.ROLE_PATIENT ) ) {
            throw new IllegalArgumentException( "The patient to search with must have the patient role" );
        }
        if ( !patientAdvocate.getRoles().contains( Role.ROLE_ADVOCATE ) ) {
            throw new IllegalArgumentException(
                    "The patient advocate to search with must have the patient advocate role" );
        }
        return repository.findByPatientAndPatientAdvocate( patient, patientAdvocate );
    }

}
