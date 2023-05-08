package edu.ncsu.csc.iTrust2.models;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import org.hibernate.annotations.Type;

import com.fasterxml.jackson.annotation.JsonProperty;

import edu.ncsu.csc.iTrust2.models.enums.Role;

/**
 * This model represents an Assignment between a Patient and a Patient Advocate
 *
 * @author ropeace
 *
 */
@Entity
public class PatientAdvocateAssignment extends DomainObject {

    /** Id for the assignment **/
    @Id
    @GeneratedValue
    private Long    id;

    /** The patient who is assigned an advocate **/
    @OneToOne
    private User    patient;

    /** The advocate who is assigned to a patient **/
    @OneToOne
    private User    patientAdvocate;

    /** The advocate can view office visit information for the patient **/
    @Type ( type = "true_false" )
    private boolean officeVisitPermission;

    /** The advocate can view billing information for the patient **/
    @Type ( type = "true_false" )
    private boolean billingPermission;

    /** The advocate can view prescription information for the patient **/
    @Type ( type = "true_false" )
    private boolean prescriptionPermission;

    /**
     * Empty constructor for hibernate
     */
    public PatientAdvocateAssignment () {
    }

    /**
     * Constructs an assignment between a patient and patient advocate with no
     * permissions
     *
     * @param patient
     *            the patient being assigned
     * @param patientAdvocate
     *            the advocate the patient is assigned to
     */
    public PatientAdvocateAssignment ( final User patient, final User patientAdvocate ) {
        setPatient( patient );
        setPatientAdvocate( patientAdvocate );
        setOfficeVisitPermission( false );
        setBillingPermission( false );
        setPrescriptionPermission( false );
    }

    /**
     * Constructs an assignment between a patient and patient advocate
     *
     * @param patient
     *            the patient being assigned
     * @param patientAdvocate
     *            the advocate the patient is assigned to
     * @param officeVisitPermission
     *            permission for viewing office visits
     * @param billingPermission
     *            permission for viewing billing information
     * @param prescriptionPermission
     *            permission for viewing prescription information
     */
    public PatientAdvocateAssignment ( final User patient, final User patientAdvocate,
            final boolean officeVisitPermission, final boolean billingPermission,
            final boolean prescriptionPermission ) {
        setPatient( patient );
        setPatientAdvocate( patientAdvocate );
        setOfficeVisitPermission( officeVisitPermission );
        setBillingPermission( billingPermission );
        setPrescriptionPermission( prescriptionPermission );

    }

    /**
     * Gets the patient in the assignment
     *
     * @return the patient
     */
    public User getPatient () {
        return patient;
    }

    /**
     * Sets the patient in the assignment if the role is patient
     *
     * @param patient
     *            the user to set
     */
    private void setPatient ( final User patient ) {
        // The patient must have the corresponding role
        if ( !patient.getRoles().contains( Role.ROLE_PATIENT ) ) {
            throw new IllegalArgumentException(
                    "The user representing the patient in the assignment must have the patient role." );
        }

        this.patient = patient;
    }

    /**
     * Gets the patient advocate in the assignment
     *
     * @return the patient advocate
     */
    public User getPatientAdvocate () {
        return patientAdvocate;
    }

    /**
     * Sets the patient advocate in the assignment if the role is patient
     * advocate
     *
     * @param patientAdvocate
     *            the user to set
     */
    private void setPatientAdvocate ( final User patientAdvocate ) {
        // A patient advocate must have the corresponding role
        if ( !patientAdvocate.getRoles().contains( Role.ROLE_ADVOCATE ) ) {
            throw new IllegalArgumentException(
                    "The user representing the patient advocate in the assignment must have the patient advocate role." );
        }
        // A patient advocate cannot be an HCP
        if ( patientAdvocate.getRoles().contains( Role.ROLE_HCP ) ) {
            throw new IllegalArgumentException( "A patient advocate cannot be a HCP or patient" );
        }

        this.patientAdvocate = patientAdvocate;
    }

    /**
     * Returns whether the advocate has permissions to view the patient's office
     * visit information
     *
     * @return boolean the permission
     */
    @JsonProperty ( "office_visit_permission" )
    public boolean getOfficeVisitPermission () {
        return officeVisitPermission;
    }

    /**
     * Sets permissions for office visits
     *
     * @param officeVisitPermission
     *            the permission
     */
    public void setOfficeVisitPermission ( final boolean officeVisitPermission ) {
        this.officeVisitPermission = officeVisitPermission;
    }

    /**
     * Returns whether the advocate has permissions to view the patient's
     * billing information
     *
     * @return boolean the permission
     */
    @JsonProperty ( "billing_permission" )
    public boolean getBillingPermission () {
        return billingPermission;
    }

    /**
     * Sets permissions for billing
     *
     * @param billingPermission
     *            the permission
     */
    public void setBillingPermission ( final boolean billingPermission ) {
        this.billingPermission = billingPermission;
    }

    /**
     * Returns whether the advocate has permissions to view the patient's
     * prescription information
     *
     * @return boolean the permission
     */
    @JsonProperty ( "prescription_permission" )
    public boolean getPrescriptionPermission () {
        return prescriptionPermission;
    }

    /**
     * Sets permissions for prescriptions
     *
     * @param prescriptionPermission
     *            the permission
     */
    public void setPrescriptionPermission ( final boolean prescriptionPermission ) {
        this.prescriptionPermission = prescriptionPermission;
    }

    @Override
    public Serializable getId () {
        return this.id;
    }

    @Override
    public String toString () {
        return patientAdvocate.getUsername() + " -> " + patient.getUsername();
    }

    @Override
    public int hashCode () {
        return Objects.hash( patient.getUsername(), patientAdvocate.getUsername() );
    }

    @Override
    public boolean equals ( final Object obj ) {
        if ( this == obj ) {
            return true;
        }
        if ( obj == null ) {
            return false;
        }
        if ( getClass() != obj.getClass() ) {
            return false;
        }
        final PatientAdvocateAssignment other = (PatientAdvocateAssignment) obj;
        return Objects.equals( id, other.id ) && Objects.equals( patient, other.patient )
                && Objects.equals( patientAdvocate, other.patientAdvocate );
    }
}
