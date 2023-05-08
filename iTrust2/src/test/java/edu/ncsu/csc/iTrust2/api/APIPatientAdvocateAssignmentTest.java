package edu.ncsu.csc.iTrust2.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.transaction.Transactional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import edu.ncsu.csc.iTrust2.common.TestUtils;
import edu.ncsu.csc.iTrust2.forms.UserForm;
import edu.ncsu.csc.iTrust2.models.Patient;
import edu.ncsu.csc.iTrust2.models.PatientAdvocateAssignment;
import edu.ncsu.csc.iTrust2.models.Personnel;
import edu.ncsu.csc.iTrust2.models.User;
import edu.ncsu.csc.iTrust2.models.enums.Role;
import edu.ncsu.csc.iTrust2.services.PatientAdvocateAssignmentService;
import edu.ncsu.csc.iTrust2.services.UserService;

/**
 * Test class for testing the API Patient Advocate Assignment Test
 *
 * @author Pavan Gandhi phgandh3 V1.0
 */
@ExtendWith ( SpringExtension.class )
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles ( { "test" } )
public class APIPatientAdvocateAssignmentTest {
    /**
     * mvc needed for testing
     */
    @Autowired
    private MockMvc                          mvc;
    /**
     * User service
     */
    @Autowired
    private UserService<User>                userService;
    /**
     * Patient advocate assignment service
     */
    @Autowired
    private PatientAdvocateAssignmentService assignmentService;

    /**
     * Set up method to clear the repositories
     */
    @BeforeEach
    public void setup () {
        userService.deleteAll();
        assignmentService.deleteAll();

    }

    /**
     * Test method for GET api call of assignments by pation
     *
     * @throws Exception
     *             if issue occurs during getting
     */

    @Test
    @Transactional
    @WithMockUser ( username = "admin", roles = { "ADMIN" } )
    public void testGetAssignmentsByPatient () throws Exception {
        final User patient1 = new Patient( new UserForm( "patient", "123456", Role.ROLE_PATIENT, 1 ) );
        userService.save( patient1 );
        final User patadvo1 = new Personnel( new UserForm( "patadvo", "123456", Role.ROLE_ADVOCATE, 1 ) );
        userService.save( patadvo1 );
        final User patadvo2 = new Personnel( new UserForm( "patadvo2", "123456", Role.ROLE_ADVOCATE, 1 ) );
        userService.save( patadvo2 );

        final PatientAdvocateAssignment assignment = new PatientAdvocateAssignment( patient1, patadvo1 );
        final PatientAdvocateAssignment addAssignment = new PatientAdvocateAssignment( patient1, patadvo2 );

        assignmentService.save( assignment );
        assignmentService.save( addAssignment );
        mvc.perform( MockMvcRequestBuilders.get( "/api/v1/assignments/patient/patient" )
                .contentType( MediaType.APPLICATION_JSON ) ).andExpect( MockMvcResultMatchers.status().isOk() );
        mvc.perform( MockMvcRequestBuilders.get( "/api/v1/assignments/patient/patientnonexistent" )
                .contentType( MediaType.APPLICATION_JSON ) ).andExpect( MockMvcResultMatchers.status().isNotFound() );

    }

    /**
     * Test method for getting assignment by patient advocate
     *
     * @throws Exception
     *             if issue with getting the assignments
     */
    @Test
    @Transactional
    @WithMockUser ( username = "admin", roles = { "ADMIN" } )
    public void testGetAssignmentsByPatientAdvocate () throws Exception {
        final User patient1 = new Patient( new UserForm( "patient", "123456", Role.ROLE_PATIENT, 1 ) );
        userService.save( patient1 );
        final User patient2 = new Patient( new UserForm( "patient2", "123456", Role.ROLE_PATIENT, 1 ) );
        userService.save( patient2 );
        final User patadvo1 = new Personnel( new UserForm( "patadvo", "123456", Role.ROLE_ADVOCATE, 1 ) );
        userService.save( patadvo1 );
        final User hcp = new Personnel( new UserForm( "hcp", "123456", Role.ROLE_HCP, 1 ) );
        userService.save( hcp );
        final PatientAdvocateAssignment assignment = new PatientAdvocateAssignment( patient1, patadvo1 );
        final PatientAdvocateAssignment addAssignment = new PatientAdvocateAssignment( patient2, patadvo1 );
        assignmentService.save( assignment );
        assignmentService.save( addAssignment );
        mvc.perform( MockMvcRequestBuilders.get( "/api/v1/assignments/patientadvocate/patadvo" )
                .contentType( MediaType.APPLICATION_JSON ) ).andExpect( MockMvcResultMatchers.status().isOk() );
        mvc.perform( MockMvcRequestBuilders.get( "/api/v1/assignments/patientadvocate/patientadvocatenonexistent" )
                .contentType( MediaType.APPLICATION_JSON ) ).andExpect( MockMvcResultMatchers.status().isNotFound() );
        mvc.perform( MockMvcRequestBuilders.get( "/api/v1/assignments/patientadvocate/hcp" )
                .contentType( MediaType.APPLICATION_JSON ) ).andExpect( MockMvcResultMatchers.status().isNotFound() );
    }

    /**
     * Test the POST operation for creating a new assignemnt
     *
     * @throws Exception
     *             if issue occurs while creating the assignment in mvc
     */
    @Test
    @Transactional
    @WithMockUser ( username = "admin", roles = { "ADMIN" } )
    public void testCreateAssignment () throws Exception {
        final User patient1 = new Patient( new UserForm( "patient", "123456", Role.ROLE_PATIENT, 1 ) );
        userService.save( patient1 );
        final User patient2 = new Patient( new UserForm( "patient2", "123456", Role.ROLE_PATIENT, 1 ) );
        userService.save( patient2 );
        final User patadvo1 = new Personnel( new UserForm( "patadvo", "123456", Role.ROLE_ADVOCATE, 1 ) );
        userService.save( patadvo1 );

        assertEquals( 0, assignmentService.count() );
        final HashMap<String, String> map = new HashMap<String, String>();
        map.put( "patUsername", "patient" );
        map.put( "patadvUsername", "patadvo" );

        final String jsonMap = TestUtils.asJsonString( map );
        final HashMap<String, String> errormap = new HashMap<String, String>();
        map.put( "patUsername", "patientnonexist" );
        map.put( "patadvUsername", "patadvonnexist" );
        final String errorMapJson = TestUtils.asJsonString( errormap );

        mvc.perform( MockMvcRequestBuilders.post( "/api/v1/assignments" ).with( csrf() )
                .contentType( MediaType.APPLICATION_JSON ).content( jsonMap ) )
                .andExpect( MockMvcResultMatchers.status().isOk() );
        assertEquals( 1, assignmentService.count() );
        mvc.perform( MockMvcRequestBuilders.post( "/api/v1/assignments" ).with( csrf() )
                .contentType( MediaType.APPLICATION_JSON ).content( errorMapJson ) )
                .andExpect( MockMvcResultMatchers.status().isNotFound() );
        assertEquals( 1, assignmentService.count() );
    }

    /**
     * Test the PUT operation for editing permissions in a assignment as an
     * admin
     *
     * @throws Exception
     *             if issue occurs while creating the assignment in mvc
     */
    @Test
    @Transactional
    @WithMockUser ( username = "admin", roles = { "ADMIN" } )
    public void testEditPermissionsAdmin () throws Exception {

        final User patient1 = new Patient( new UserForm( "patient", "123456", Role.ROLE_PATIENT, 1 ) );
        userService.save( patient1 );
        final User patient2 = new Patient( new UserForm( "patient2", "123456", Role.ROLE_PATIENT, 1 ) );
        userService.save( patient2 );
        final User patadvo1 = new Personnel( new UserForm( "patadvo", "123456", Role.ROLE_ADVOCATE, 1 ) );
        userService.save( patadvo1 );

        assertEquals( 0, assignmentService.count() );
        final PatientAdvocateAssignment assignment = new PatientAdvocateAssignment( patient1, patadvo1 );
        assignmentService.save( assignment );
        final List<Boolean> list = new ArrayList<Boolean>();
        list.add( true );
        list.add( true );
        list.add( true );

        mvc.perform( MockMvcRequestBuilders
                .put( "/api/v1/assignments/permissions/" + patient1.getUsername() + "/" + patadvo1.getUsername() )
                .with( csrf() ).contentType( MediaType.APPLICATION_JSON ).content( TestUtils.asJsonString( list ) ) )
                .andExpect( MockMvcResultMatchers.status().isOk() );
        assertEquals( 1, assignmentService.count() );
        assertTrue( assignment.getBillingPermission() );
        assertTrue( assignment.getOfficeVisitPermission() );
        assertTrue( assignment.getPrescriptionPermission() );

        mvc.perform( MockMvcRequestBuilders
                .put( "/api/v1/assignments/permissions/badpatient/" + patadvo1.getUsername() ).with( csrf() )
                .contentType( MediaType.APPLICATION_JSON ).content( TestUtils.asJsonString( list ) ) )
                .andExpect( MockMvcResultMatchers.status().isNotFound() );
        mvc.perform( MockMvcRequestBuilders
                .put( "/api/v1/assignments/permissions/" + patient1.getUsername() + "/badadvocate" ).with( csrf() )
                .contentType( MediaType.APPLICATION_JSON ).content( TestUtils.asJsonString( list ) ) )
                .andExpect( MockMvcResultMatchers.status().isNotFound() );
        mvc.perform( MockMvcRequestBuilders
                .put( "/api/v1/assignments/permissions/" + patient2.getUsername() + "/" + patadvo1.getUsername() )
                .with( csrf() ).contentType( MediaType.APPLICATION_JSON ).content( TestUtils.asJsonString( list ) ) )
                .andExpect( MockMvcResultMatchers.status().isNotFound() );
        list.remove( 0 );
        mvc.perform( MockMvcRequestBuilders
                .put( "/api/v1/assignments/permissions/" + patient1.getUsername() + "/" + patadvo1.getUsername() )
                .with( csrf() ).contentType( MediaType.APPLICATION_JSON ).content( TestUtils.asJsonString( list ) ) )
                .andExpect( MockMvcResultMatchers.status().isBadRequest() );

    }

    /**
     * Test the PUT operation for editing permissions in a assignment as an
     * patient
     *
     * @throws Exception
     *             if issue occurs while creating the assignment in mvc
     */
    @Test
    @Transactional
    @WithMockUser ( username = "patient1", roles = { "PATIENT" } )
    public void testEditPermissionsPatient () throws Exception {
        final User patient1 = new Patient( new UserForm( "patient", "123456", Role.ROLE_PATIENT, 1 ) );
        userService.save( patient1 );
        final User patient2 = new Patient( new UserForm( "patient2", "123456", Role.ROLE_PATIENT, 1 ) );
        userService.save( patient2 );
        final User patadvo1 = new Personnel( new UserForm( "patadvo", "123456", Role.ROLE_ADVOCATE, 1 ) );
        userService.save( patadvo1 );

        assertEquals( 0, assignmentService.count() );
        final PatientAdvocateAssignment assignment = new PatientAdvocateAssignment( patient1, patadvo1 );
        assignmentService.save( assignment );
        final List<Boolean> list = new ArrayList<Boolean>();
        list.add( true );
        list.add( true );
        list.add( true );

        mvc.perform( MockMvcRequestBuilders
                .put( "/api/v1/assignments/permissions/" + patient1.getUsername() + "/" + patadvo1.getUsername() )
                .with( csrf() ).contentType( MediaType.APPLICATION_JSON ).content( TestUtils.asJsonString( list ) ) )
                .andExpect( MockMvcResultMatchers.status().isOk() );
        assertEquals( 1, assignmentService.count() );
        assertTrue( assignment.getBillingPermission() );
        assertTrue( assignment.getOfficeVisitPermission() );
        assertTrue( assignment.getPrescriptionPermission() );

        mvc.perform( MockMvcRequestBuilders
                .put( "/api/v1/assignments/permissions/badpatient/" + patadvo1.getUsername() ).with( csrf() )
                .contentType( MediaType.APPLICATION_JSON ).content( TestUtils.asJsonString( list ) ) )
                .andExpect( MockMvcResultMatchers.status().isNotFound() );
        mvc.perform( MockMvcRequestBuilders
                .put( "/api/v1/assignments/permissions/" + patient1.getUsername() + "/badadvocate" ).with( csrf() )
                .contentType( MediaType.APPLICATION_JSON ).content( TestUtils.asJsonString( list ) ) )
                .andExpect( MockMvcResultMatchers.status().isNotFound() );
        mvc.perform( MockMvcRequestBuilders
                .put( "/api/v1/assignments/permissions/" + patient2.getUsername() + "/" + patadvo1.getUsername() )
                .with( csrf() ).contentType( MediaType.APPLICATION_JSON ).content( TestUtils.asJsonString( list ) ) )
                .andExpect( MockMvcResultMatchers.status().isNotFound() );
        list.remove( 0 );
        mvc.perform( MockMvcRequestBuilders
                .put( "/api/v1/assignments/permissions/" + patient1.getUsername() + "/" + patadvo1.getUsername() )
                .with( csrf() ).contentType( MediaType.APPLICATION_JSON ).content( TestUtils.asJsonString( list ) ) )
                .andExpect( MockMvcResultMatchers.status().isBadRequest() );

    }
}
