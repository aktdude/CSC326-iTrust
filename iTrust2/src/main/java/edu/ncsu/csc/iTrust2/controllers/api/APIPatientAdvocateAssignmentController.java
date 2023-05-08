package edu.ncsu.csc.iTrust2.controllers.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import edu.ncsu.csc.iTrust2.models.Patient;
import edu.ncsu.csc.iTrust2.models.PatientAdvocateAssignment;
import edu.ncsu.csc.iTrust2.models.User;
import edu.ncsu.csc.iTrust2.models.enums.Role;
import edu.ncsu.csc.iTrust2.models.enums.TransactionType;
import edu.ncsu.csc.iTrust2.services.PatientAdvocateAssignmentService;
import edu.ncsu.csc.iTrust2.services.PatientService;
import edu.ncsu.csc.iTrust2.services.UserService;
import edu.ncsu.csc.iTrust2.utils.LoggerUtil;

/**
 * Class that provides end points for interacting with the
 * PatientAdvocateAssignment model
 *
 * @author Pavan Gandhi (phgandh3)
 *
 */
@RestController
@SuppressWarnings ( { "unchecked", "rawtypes" } )
public class APIPatientAdvocateAssignmentController extends APIController {
    /**
     * User service
     */
    @Autowired
    private UserService                      service;
    /**
     * Patient Service
     */
    @Autowired
    private PatientService                   patientService;

    /**
     * Logging utility
     */
    @Autowired
    private LoggerUtil                       loggerUtil;

    /**
     * PatientAdvocateAssignment service
     */
    @Autowired
    private PatientAdvocateAssignmentService assignmentService;

    /**
     * GET operation for getting a list of all of the assignments in the
     * database
     *
     * @return List<> a list of all the assignments
     */

    @GetMapping ( BASE_PATH + "/assignments" )
    public List<PatientAdvocateAssignment> getAssignments () {
        loggerUtil.log( TransactionType.PATIENT_ADVOCATE_ASSIGNMENTS_VIEWED, LoggerUtil.currentUser() );
        return assignmentService.findAll();
    }

    /**
     * GET operation to find assignments given a patient username
     *
     * @param username
     *            the username of the patient
     * @return ResponseEntity with a list of related assignments and http status
     */

    @GetMapping ( BASE_PATH + "/assignments/patient/{username}" )
    public ResponseEntity getAssignmentsByPatient ( @PathVariable ( "username" ) final String username ) {
        final User given = service.findByName( username );
        if ( given == null ) {
            return new ResponseEntity( errorResponse( "Could not find a user with username " + username ),
                    HttpStatus.NOT_FOUND );
        }
        final Patient patient = (Patient) patientService.findByName( given.getUsername() );
        if ( patient == null ) {
            return new ResponseEntity( errorResponse( "Could not find a patient with username " + given.getUsername() ),
                    HttpStatus.NOT_FOUND );
        }
        final List<PatientAdvocateAssignment> assignments = assignmentService.getAssignmentsByPatient( given );
        loggerUtil.log( TransactionType.PATIENT_ADVOCATE_ASSIGNMENTS_VIEWED, LoggerUtil.currentUser() );
        return new ResponseEntity( assignments, HttpStatus.OK );
    }

    /**
     * GET operation for getting assignments by patient advocate username
     *
     * @param username
     *            the username of the patient advocate to find results for
     * @return ResponseEntity with a list of the related assignments and a
     *         status code
     */

    @GetMapping ( BASE_PATH + "/assignments/patientadvocate/{username}" )
    public ResponseEntity getAssignmentsByPatientAdvocate ( @PathVariable ( "username" ) final String username ) {
        final User given = service.findByName( username );
        if ( given == null ) {
            return new ResponseEntity( errorResponse( "Could not find a user with username " + username ),
                    HttpStatus.NOT_FOUND );
        }
        if ( !given.getRoles().contains( Role.ROLE_ADVOCATE ) ) {
            return new ResponseEntity(
                    errorResponse( "Could not find a patient advocate with username " + given.getUsername() ),
                    HttpStatus.NOT_FOUND );
        }

        final List<PatientAdvocateAssignment> assignments = assignmentService.getAssignmentsByPatientAdvocate( given );
        loggerUtil.log( TransactionType.PATIENT_ADVOCATE_ASSIGNMENTS_VIEWED, LoggerUtil.currentUser() );
        return new ResponseEntity( assignments, HttpStatus.OK );
    }

    /**
     * GET operation to find assignments given a patient username
     *
     * @param username
     *            the username of the patient
     * @return ResponseEntity with a list of advocates and their usernames with
     *         a http status
     */

    @GetMapping ( BASE_PATH + "/advocates/patient/{username}" )
    public ResponseEntity getAdvocatesByPatient ( @PathVariable ( "username" ) final String username ) {
        final User given = service.findByName( username );
        if ( given == null ) {
            return new ResponseEntity( errorResponse( "Could not find a user with username " + username ),
                    HttpStatus.NOT_FOUND );
        }
        final Patient patient = (Patient) patientService.findByName( given.getUsername() );
        if ( patient == null ) {
            return new ResponseEntity( errorResponse( "Could not find a patient with username " + given.getUsername() ),
                    HttpStatus.NOT_FOUND );
        }
        final List<PatientAdvocateAssignment> assignments = assignmentService.getAssignmentsByPatient( given );
        final List<String> advocates = new ArrayList<String>();
        for ( int i = 0; i < assignments.size(); i++ ) {
            advocates.add( assignments.get( i ).getPatientAdvocate().getUsername() );
        }
        return new ResponseEntity( advocates, HttpStatus.OK );
    }

    /**
     * POST operation for creating an assignment provded a map with key as the
     * name of the username type and value as username
     *
     * @param usernames
     *            the map containing the type of username and username
     * @return ResponseEntity containing httpStatus and the created assignment
     */
    @PostMapping ( BASE_PATH + "/assignments" )
    @PreAuthorize ( "hasAnyRole('ROLE_ADMIN')" )
    public ResponseEntity createAssignment ( @RequestBody final Map<String, String> usernames ) {
        final User givenPatient = service.findByName( usernames.get( "patUsername" ) );
        if ( givenPatient == null ) {
            return new ResponseEntity(
                    errorResponse( "Could not find a user with username " + usernames.get( "patUsername" ) ),
                    HttpStatus.NOT_FOUND );
        }
        if ( !givenPatient.getRoles().contains( Role.ROLE_PATIENT ) ) {
            return new ResponseEntity(
                    errorResponse( "The username provided is not a valid patient" + givenPatient.getUsername() ),
                    HttpStatus.NOT_FOUND );
        }
        final User givenPatientAdvocate = service.findByName( usernames.get( "patadvUsername" ) );
        if ( givenPatientAdvocate == null ) {
            return new ResponseEntity(
                    errorResponse( "Could not find a user with username " + usernames.get( "patadvUsername" ) ),
                    HttpStatus.NOT_FOUND );
        }
        if ( !givenPatientAdvocate.getRoles().contains( Role.ROLE_ADVOCATE ) ) {
            return new ResponseEntity( errorResponse(
                    "The username provided is not a valid patient advocate" + givenPatientAdvocate.getUsername() ),
                    HttpStatus.NOT_FOUND );
        }
        final PatientAdvocateAssignment patientAssign = new PatientAdvocateAssignment( givenPatient,
                givenPatientAdvocate );
        assignmentService.save( patientAssign );
        loggerUtil.log( TransactionType.PATIENT_ADVOCATE_ASSIGNMENT_CREATED, LoggerUtil.currentUser() );
        return new ResponseEntity( patientAssign, HttpStatus.OK );
    }

    /**
     * PUT operation for editing permissions of a patient advocate in a given
     * association
     *
     * @param patUsername
     *            the username of the patient in the assignment
     * @param advUsername
     *            the username of the advocate in the assignment
     * @param perms
     *            the list of permissions to set in the assignment
     * @return ResponseEntity containing httpStatus and the edited assignment
     *         with permissions
     */
    @PutMapping ( BASE_PATH + "/assignments/permissions/{patUsername}/{advUsername}" )
    @PreAuthorize ( "hasAnyRole('ROLE_ADMIN', 'ROLE_PATIENT')" )
    public ResponseEntity editPermissions ( @PathVariable ( "patUsername" ) final String patUsername,
            @PathVariable ( "advUsername" ) final String advUsername, @RequestBody final List<Boolean> perms ) {
        final User pat = service.findByName( patUsername );
        if ( pat == null ) {
            return new ResponseEntity( errorResponse( "Could not find a user with username " + patUsername ),
                    HttpStatus.NOT_FOUND );
        }
        final Patient patient = (Patient) patientService.findByName( pat.getUsername() );
        if ( patient == null ) {
            return new ResponseEntity( errorResponse( "Could not find a patient with username " + pat.getUsername() ),
                    HttpStatus.NOT_FOUND );
        }

        final User adv = service.findByName( advUsername );
        if ( adv == null ) {
            return new ResponseEntity( errorResponse( "Could not find a user with username " + adv ),
                    HttpStatus.NOT_FOUND );
        }
        if ( !adv.getRoles().contains( Role.ROLE_ADVOCATE ) ) {
            return new ResponseEntity(
                    errorResponse( "Could not find a patient advocate with username " + adv.getUsername() ),
                    HttpStatus.NOT_FOUND );
        }

        final PatientAdvocateAssignment assignment = assignmentService.getAssignment( patient, adv );

        if ( assignment == null ) {
            return new ResponseEntity( errorResponse(
                    "Could not find an association between " + adv.getUsername() + " and " + patient.getUsername() ),
                    HttpStatus.NOT_FOUND );
        }

        if ( perms.size() != 3 ) {
            return new ResponseEntity( errorResponse( "The permissions provided are invalid" ),
                    HttpStatus.BAD_REQUEST );
        }

        final int officeVisitIndex = 0;
        final int billingIndex = 1;
        final int prescriptionIndex = 2;

        assignment.setOfficeVisitPermission( perms.get( officeVisitIndex ) );
        assignment.setBillingPermission( perms.get( billingIndex ) );
        assignment.setPrescriptionPermission( perms.get( prescriptionIndex ) );
        assignmentService.save( assignment );

        return new ResponseEntity( assignment, HttpStatus.OK );
    }
}
