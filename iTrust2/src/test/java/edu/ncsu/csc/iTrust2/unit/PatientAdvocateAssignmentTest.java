package edu.ncsu.csc.iTrust2.unit;

import java.util.List;

import javax.transaction.Transactional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import edu.ncsu.csc.iTrust2.TestConfig;
import edu.ncsu.csc.iTrust2.forms.UserForm;
import edu.ncsu.csc.iTrust2.models.Patient;
import edu.ncsu.csc.iTrust2.models.PatientAdvocateAssignment;
import edu.ncsu.csc.iTrust2.models.Personnel;
import edu.ncsu.csc.iTrust2.models.User;
import edu.ncsu.csc.iTrust2.models.enums.Role;
import edu.ncsu.csc.iTrust2.services.PatientAdvocateAssignmentService;
import edu.ncsu.csc.iTrust2.services.UserService;

/**
 * Class to test the PatientAdvocateAssignment class
 *
 * @author ropeace
 *
 */
@ExtendWith ( SpringExtension.class )
@EnableAutoConfiguration
@SpringBootTest ( classes = TestConfig.class )
@ActiveProfiles ( { "test" } )
@SuppressWarnings ( { "rawtypes, unchecked" } )
public class PatientAdvocateAssignmentTest {

    /**
     * PatientAdvocateAssignmentService
     */
    @Autowired
    private PatientAdvocateAssignmentService service;

    /**
     * UserService
     */
    @Autowired
    private UserService<User>                userService;

    /** Advocate username for testing **/
    private static final String              ADVOCATE_1 = "testAdvocate1";
    /** Advocate username for testing **/
    private static final String              ADVOCATE_2 = "testAdvocate2";
    /** Advocate username for testing **/
    private static final String              ADVOCATE_3 = "testAdvocate3";
    /** Patient username for testing **/
    private static final String              PATIENT_1  = "testPatient1";
    /** Patient username for testing **/
    private static final String              PATIENT_2  = "testPatient2";
    /** Patient username for testing **/
    private static final String              PATIENT_3  = "testPatient3";
    /** Test password **/
    private static final String              PW         = "123456";

    /**
     * Clear the database before the test is run
     */
    @BeforeEach
    public void setup () {
        service.deleteAll();
        userService.deleteAll();
    }

    /**
     * Testing valid in invalid cases for assigning advocates to patients
     */
    @Test
    @Transactional
    public void testAssignments () {
        Assertions.assertEquals( 0, service.count(), "There should be no assignments in the system" );
        Assertions.assertEquals( 0, userService.count(), "There should be no users in the system" );

        // Create Advocates and Patients
        final User advocate1 = new Personnel( new UserForm( ADVOCATE_1, PW, Role.ROLE_ADVOCATE, 1 ) );
        final User advocate2 = new Personnel( new UserForm( ADVOCATE_2, PW, Role.ROLE_ADVOCATE, 1 ) );
        final User patient1 = new Patient( new UserForm( PATIENT_1, PW, Role.ROLE_PATIENT, 1 ) );
        final User patient2 = new Patient( new UserForm( PATIENT_2, PW, Role.ROLE_PATIENT, 1 ) );

        userService.saveAll( List.of( advocate1, advocate2, patient1, patient2 ) );

        Assertions.assertEquals( 4, userService.count() );

        final PatientAdvocateAssignment a1ToP1 = new PatientAdvocateAssignment( patient1, advocate1 );
        service.save( a1ToP1 );

        Assertions.assertEquals( 1, service.count() );
        Assertions.assertEquals( advocate1, service.getAssignmentsByPatient( patient1 ).get( 0 ).getPatientAdvocate() );

        final PatientAdvocateAssignment a2ToP1 = new PatientAdvocateAssignment( patient1, advocate2 );
        service.save( a2ToP1 );

        Assertions.assertEquals( 2, service.count() );
        Assertions.assertEquals( advocate1, service.getAssignmentsByPatient( patient1 ).get( 0 ).getPatientAdvocate() );
        Assertions.assertEquals( advocate2, service.getAssignmentsByPatient( patient1 ).get( 1 ).getPatientAdvocate() );

        final PatientAdvocateAssignment a2ToP2 = new PatientAdvocateAssignment( patient2, advocate2 );
        service.save( a2ToP2 );
        Assertions.assertEquals( 3, service.count() );
        Assertions.assertEquals( patient1, service.getAssignmentsByPatientAdvocate( advocate2 ).get( 0 ).getPatient() );
        Assertions.assertEquals( patient2, service.getAssignmentsByPatientAdvocate( advocate2 ).get( 1 ).getPatient() );
        Assertions.assertEquals( a2ToP2, service.getAssignment( patient2, advocate2 ) );

        final User advocate3 = new Personnel( new UserForm( ADVOCATE_3, PW, Role.ROLE_ADVOCATE, 1 ) );
        final User patient3 = new Patient( new UserForm( PATIENT_3, PW, Role.ROLE_PATIENT, 1 ) );

        // Should not find any patient advocate assignment here because it was
        // never created.
        Assertions.assertTrue( service.getAssignmentsByPatient( patient3 ).isEmpty() );
        Assertions.assertTrue( service.getAssignmentsByPatientAdvocate( advocate3 ).isEmpty() );
        Assertions.assertNull( service.getAssignment( patient3, advocate3 ) );

        // Invalid case: Cannot assign advocates to advocates
        PatientAdvocateAssignment shouldFail = null;
        try {
            shouldFail = new PatientAdvocateAssignment( advocate3, advocate3 );
        }
        catch ( final IllegalArgumentException e ) {
            Assertions.assertNull( shouldFail );
        }

        // Invalid case: Cannot assign patients to patients
        try {
            shouldFail = new PatientAdvocateAssignment( patient3, patient3 );
        }
        catch ( final IllegalArgumentException e ) {
            Assertions.assertNull( shouldFail );
        }

        // Invalid case: can't get patient assignments from an advocate
        Assertions.assertThrows( IllegalArgumentException.class, () -> {
            service.getAssignmentsByPatient( advocate3 );
        } );

        // Invalid case: can't get advocate assignments from a patient
        Assertions.assertThrows( IllegalArgumentException.class, () -> {
            service.getAssignmentsByPatientAdvocate( patient3 );
        } );

        // Invalid case: can't get an assignment using two patients
        Assertions.assertThrows( IllegalArgumentException.class, () -> {
            service.getAssignment( patient3, patient3 );
        } );

        // Invalid case: can't get an assignment from two advocates
        Assertions.assertThrows( IllegalArgumentException.class, () -> {
            service.getAssignment( advocate3, advocate3 );
        } );

        final User invalidAdvocate = new Personnel( new UserForm( ADVOCATE_1, PW, Role.ROLE_ADVOCATE, 1 ) );
        invalidAdvocate.addRole( Role.ROLE_HCP );

        Assertions.assertThrows( IllegalArgumentException.class, () -> {
            new PatientAdvocateAssignment( patient1, invalidAdvocate );
        } );
    }

    /**
     * Testing the permissions functionality for patient advocate assignments
     */
    @Test
    @Transactional
    public void testPermissions () {
        Assertions.assertEquals( 0, service.count(), "There should be no assignments in the system" );
        Assertions.assertEquals( 0, userService.count(), "There should be no users in the system" );

        // Create Advocates and Patients
        final User advocate1 = new Personnel( new UserForm( ADVOCATE_1, PW, Role.ROLE_ADVOCATE, 1 ) );
        final User advocate2 = new Personnel( new UserForm( ADVOCATE_2, PW, Role.ROLE_ADVOCATE, 1 ) );
        final User patient1 = new Patient( new UserForm( PATIENT_1, PW, Role.ROLE_PATIENT, 1 ) );
        final User patient2 = new Patient( new UserForm( PATIENT_2, PW, Role.ROLE_PATIENT, 1 ) );

        userService.saveAll( List.of( advocate1, advocate2, patient1, patient2 ) );

        Assertions.assertEquals( 4, userService.count() );

        PatientAdvocateAssignment a1ToP1 = new PatientAdvocateAssignment( patient1, advocate1 );
        service.save( a1ToP1 );

        Assertions.assertEquals( 1, service.count() );
        Assertions.assertFalse( service.getAssignment( patient1, advocate1 ).getOfficeVisitPermission() );
        Assertions.assertFalse( service.getAssignment( patient1, advocate1 ).getBillingPermission() );
        Assertions.assertFalse( service.getAssignment( patient1, advocate1 ).getPrescriptionPermission() );

        service.deleteAll();

        a1ToP1 = new PatientAdvocateAssignment( patient1, advocate1, true, true, true );
        service.save( a1ToP1 );

        Assertions.assertEquals( 1, service.count() );
        Assertions.assertTrue( service.getAssignment( patient1, advocate1 ).getOfficeVisitPermission() );
        Assertions.assertTrue( service.getAssignment( patient1, advocate1 ).getBillingPermission() );
        Assertions.assertTrue( service.getAssignment( patient1, advocate1 ).getPrescriptionPermission() );

    }

    /**
     * Tests additional methods in the model class such as equals, hashCode and
     * toString
     */
    @Test
    @Transactional
    public void testAssignmentModel () {
        Assertions.assertEquals( 0, service.count(), "There should be no assignments in the system" );
        Assertions.assertEquals( 0, userService.count(), "There should be no users in the system" );

        final User advocate1 = new Personnel( new UserForm( ADVOCATE_1, PW, Role.ROLE_ADVOCATE, 1 ) );
        final User advocate2 = new Personnel( new UserForm( ADVOCATE_2, PW, Role.ROLE_ADVOCATE, 1 ) );
        final User patient1 = new Patient( new UserForm( PATIENT_1, PW, Role.ROLE_PATIENT, 1 ) );
        final User patient2 = new Patient( new UserForm( PATIENT_2, PW, Role.ROLE_PATIENT, 1 ) );

        userService.saveAll( List.of( advocate1, advocate2, patient1, patient2 ) );
        Assertions.assertEquals( 4, userService.count() );

        final PatientAdvocateAssignment a1ToP1 = new PatientAdvocateAssignment( patient1, advocate1 );
        final PatientAdvocateAssignment a1ToP2 = new PatientAdvocateAssignment( patient2, advocate1 );
        final PatientAdvocateAssignment a2ToP1 = new PatientAdvocateAssignment( patient1, advocate2 );
        final PatientAdvocateAssignment a2ToP2 = new PatientAdvocateAssignment( patient2, advocate2 );

        Assertions.assertTrue( a1ToP1.equals( a1ToP1 ) );
        Assertions.assertFalse( a1ToP1.equals( a2ToP1 ) );
        Assertions.assertFalse( a1ToP1.equals( a1ToP2 ) );
        Assertions.assertFalse( a1ToP1.equals( a2ToP2 ) );
        Assertions.assertFalse( a1ToP1.equals( "string" ) );
        Assertions.assertFalse( a1ToP1.equals( null ) );

        Assertions.assertEquals( 577292471, a1ToP1.hashCode() );
        Assertions.assertEquals( 577292503, a2ToP2.hashCode() );
        Assertions.assertEquals( "testAdvocate1 -> testPatient1", a1ToP1.toString() );
        Assertions.assertEquals( "testAdvocate2 -> testPatient2", a2ToP2.toString() );

    }
}
