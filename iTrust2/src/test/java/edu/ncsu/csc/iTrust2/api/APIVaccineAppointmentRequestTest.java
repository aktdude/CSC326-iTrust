package edu.ncsu.csc.iTrust2.api;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.junit.jupiter.api.Assertions;
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

import edu.ncsu.csc.iTrust2.common.TestUtils;
import edu.ncsu.csc.iTrust2.forms.UserForm;
import edu.ncsu.csc.iTrust2.forms.VaccineAppointmentRequestForm;
import edu.ncsu.csc.iTrust2.models.Patient;
import edu.ncsu.csc.iTrust2.models.PatientAdvocateAssignment;
import edu.ncsu.csc.iTrust2.models.Personnel;
import edu.ncsu.csc.iTrust2.models.User;
import edu.ncsu.csc.iTrust2.models.VaccineAppointmentRequest;
import edu.ncsu.csc.iTrust2.models.VaccineType;
import edu.ncsu.csc.iTrust2.models.enums.AppointmentType;
import edu.ncsu.csc.iTrust2.models.enums.Role;
import edu.ncsu.csc.iTrust2.models.enums.Status;
import edu.ncsu.csc.iTrust2.models.enums.VaccinationStatus;
import edu.ncsu.csc.iTrust2.services.PatientAdvocateAssignmentService;
import edu.ncsu.csc.iTrust2.services.UserService;
import edu.ncsu.csc.iTrust2.services.VaccineAppointmentRequestService;
import edu.ncsu.csc.iTrust2.services.VaccineTypeService;

/**
 * Tests the API for vaccine appointment requests
 *
 */
@ExtendWith ( SpringExtension.class )
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles ( { "test" } )
public class APIVaccineAppointmentRequestTest {
    @Autowired
    private MockMvc                          mvc;

    @Autowired
    private VaccineTypeService               vaccineService;

    @Autowired
    private VaccineAppointmentRequestService service;

    @Autowired
    private UserService<User>                userService;

    @Autowired
    private PatientAdvocateAssignmentService padvservice;

    /**
     * Sets up tests
     */
    @BeforeEach
    public void setup () {
        service.deleteAll();
        userService.deleteAll();
        padvservice.deleteAll();

        final User patient = new Patient( new UserForm( "patient", "123456", Role.ROLE_PATIENT, 1 ) );

        final User hcp = new Personnel( new UserForm( "hcp", "123456", Role.ROLE_HCP, 1 ) );

        final User advocate = new Personnel( new UserForm( "advocate", "123456", Role.ROLE_ADVOCATE, 1 ) );

        final PatientAdvocateAssignment assign = new PatientAdvocateAssignment( patient, advocate );

        final VaccineType vaccine = new VaccineType();
        vaccine.setName( "Moderna" );
        vaccine.setNumDoses( 2 );
        vaccine.setIsAvailable( true );

        userService.saveAll( List.of( patient, hcp, advocate ) );
        vaccineService.save( vaccine );
        padvservice.save( assign );

    }

    /**
     * Tests that getting an appointment that doesn't exist returns the proper
     * status
     *
     * @throws Exception
     */
    @Test
    @WithMockUser ( username = "hcp", roles = { "HCP" } )
    @Transactional
    public void testGetNonExistentAppointment () throws Exception {
        mvc.perform( get( "/api/v1/vaccineappointments/-1" ) ).andExpect( status().isNotFound() );
    }

    /**
     * Tests that deleting an appointment that doesn't exist returns the proper
     * status.
     */
    @Test
    @WithMockUser ( username = "hcp", roles = { "HCP" } )
    @Transactional
    public void testDeleteNonExistentAppointment () throws Exception {
        mvc.perform( delete( "/api/v1/vaccineappointments/-1" ).with( csrf() ) ).andExpect( status().isNotFound() );
    }

    /**
     * Tests creating an appointment request with bad data. Should return a bad
     * request.
     *
     * @throws Exception
     */
    @Test
    @WithMockUser ( username = "patient", roles = { "PATIENT" } )
    @Transactional
    public void testCreateBadAppointmentRequest () throws Exception {

        final VaccineAppointmentRequestForm appointmentForm = new VaccineAppointmentRequestForm();
        appointmentForm.setDate( "0" );
        appointmentForm.setType( AppointmentType.VACCINATION.toString() );
        appointmentForm.setStatus( Status.PENDING.toString() );
        appointmentForm.setHcp( "hcp" );
        appointmentForm.setPatient( "patient" );
        appointmentForm.setVaccineType( "Moderna" );
        appointmentForm.setComments( "Test appointment please ignore" );
        appointmentForm.setVaccineStatus( VaccinationStatus.NOT_VACCINATED.toString() );

        mvc.perform( post( "/api/v1/vaccineappointments" ).with( csrf() ).contentType( MediaType.APPLICATION_JSON )
                .content( TestUtils.asJsonString( appointmentForm ) ) ).andExpect( status().isBadRequest() );
    }

    /**
     * Tests AppointmentRequestAPi
     *
     * @throws Exception
     */
    @Test
    @WithMockUser ( username = "patient", roles = { "PATIENT" } )
    @Transactional
    public void testAppointmentRequestAPI () throws Exception {

        final User patient = userService.findByName( "patient" );
        final User advocate = userService.findByName( "advocate" );

        final VaccineAppointmentRequestForm appointmentForm = new VaccineAppointmentRequestForm();
        appointmentForm.setDate( "2030-11-19T04:50:00.000-05:00" ); // 2030-11-19
                                                                    // 4:50 AM
                                                                    // EST
        final List<String> advocates = new ArrayList<String>();
        advocates.add( advocate.getUsername() );

        appointmentForm.setType( AppointmentType.VACCINATION.toString() );
        appointmentForm.setStatus( Status.PENDING.toString() );
        appointmentForm.setHcp( "hcp" );
        appointmentForm.setPatient( "patient" );
        appointmentForm.setComments( "Test appointment please ignore" );
        appointmentForm.setVaccineType( "Moderna" );
        appointmentForm.setVaccineStatus( VaccinationStatus.NOT_VACCINATED.toString() );
        appointmentForm.setPatientAdvocatesInvited( advocates );

        /* Create the request */
        mvc.perform( post( "/api/v1/vaccineappointments" ).with( csrf() ).contentType( MediaType.APPLICATION_JSON )
                .content( TestUtils.asJsonString( appointmentForm ) ) ).andExpect( status().isOk() );

        mvc.perform( get( "/api/v1/appointmentrequest" ) ).andExpect( status().isOk() )
                .andExpect( content().contentType( MediaType.APPLICATION_JSON_VALUE ) );

        final List<VaccineAppointmentRequest> temp = service.findByPatient( patient );
        Assertions.assertEquals( 1, temp.size() );
        Assertions.assertEquals( 1, temp.get( 0 ).getPatientAdvocatesInvited().size() );

        /*
         * We need the ID of the appointment request that actually got _saved_
         * when calling the API above. This will get it
         */

        final Long id = service.findByPatient( patient ).get( 0 ).getId();

        mvc.perform( get( "/api/v1/vaccineappointments/" + id ) ).andExpect( status().isOk() )
                .andExpect( content().contentType( MediaType.APPLICATION_JSON_VALUE ) );

        List<VaccineAppointmentRequest> forPatient = service.findAll();
        Assertions.assertEquals( 1, forPatient.size() );

        mvc.perform( get( "/api/v1/vaccineappointments/" + id ) ).andExpect( status().isOk() )
                .andExpect( content().contentType( MediaType.APPLICATION_JSON_VALUE ) );

        forPatient = service.findAll();
        Assertions.assertEquals( 1, forPatient.size() );

        // Viewing a nonexistent ID should not work
        mvc.perform( get( "/api/v1/vaccineappointments/-1" ).contentType( MediaType.APPLICATION_JSON )
                .content( TestUtils.asJsonString( appointmentForm ) ) ).andExpect( status().isNotFound() );

        mvc.perform( delete( "/api/v1/vaccineappointments/" + id ).with( csrf() ) ).andExpect( status().isOk() );

    }

    @Test
    @Transactional
    @WithMockUser ( username = "hcp", roles = { "HCP" } )
    public void testUpdateVaccAppointmentRequest () throws Exception {
        final VaccineAppointmentRequestForm appointmentForm = new VaccineAppointmentRequestForm();
        appointmentForm.setDate( "2030-11-19T04:50:00.000-05:00" );
        appointmentForm.setType( AppointmentType.VACCINATION.toString() );
        appointmentForm.setStatus( Status.PENDING.toString() );
        appointmentForm.setHcp( "hcp" );
        appointmentForm.setPatient( "patient" );
        appointmentForm.setVaccineType( "Moderna" );
        appointmentForm.setComments( "Test appointment please ignore" );
        appointmentForm.setVaccineStatus( VaccinationStatus.NOT_VACCINATED.toString() );

        final VaccineAppointmentRequest req = service.build( appointmentForm );

        service.save( req );

        final User patient = userService.findByName( "patient" );
        final Long id = service.findByPatient( patient ).get( 0 ).getId();

        // try a valid update
        appointmentForm.setStatus( Status.APPROVED.toString() );
        mvc.perform( put( "/api/v1/vaccineappointments/" + id ).with( csrf() ).contentType( MediaType.APPLICATION_JSON )
                .content( TestUtils.asJsonString( appointmentForm ) ) ).andExpect( status().isOk() );

        final Long deliberatelyWrongId = id - 1;

        // try an update that should certainly be wrong

        mvc.perform( put( "/api/v1/vaccineappointments/" + deliberatelyWrongId ).with( csrf() )
                .contentType( MediaType.APPLICATION_JSON ).content( TestUtils.asJsonString( appointmentForm ) ) )
                .andExpect( status().isNotFound() );

        appointmentForm.setVaccineStatus( VaccinationStatus.FULLY_VACCINATED.toString() );
        mvc.perform( put( "/api/v1/vaccineappointments/" + id ).with( csrf() ).contentType( MediaType.APPLICATION_JSON )
                .content( TestUtils.asJsonString( appointmentForm ) ) ).andExpect( status().isBadRequest() );

        // make sure the valid update from before went through
        final String vaccinesFromAPI = mvc.perform( get( "/api/v1/vaccineappointments" ) ).andExpect( status().isOk() )
                .andReturn().getResponse().getContentAsString();

        final VaccineAppointmentRequest[] parsedFromAPI = TestUtils.gson().fromJson( vaccinesFromAPI,
                VaccineAppointmentRequest[].class );

        Assertions.assertEquals( 1, parsedFromAPI.length );
        Assertions.assertEquals( Status.APPROVED, parsedFromAPI[0].getStatus() );

    }
}
