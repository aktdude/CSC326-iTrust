package edu.ncsu.csc.iTrust2.controllers.routing;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import edu.ncsu.csc.iTrust2.models.enums.Role;

/**
 * Controller to manage basic abilities for Patient Advocate roles
 *
 * @author Alex Taylor (aktaylo4)
 *
 */

@Controller
public class AdvocateController {

    /**
     * Returns the advocate for the given model
     *
     * @param model
     *            model to check
     * @return role
     */
    @RequestMapping ( value = "advocate/index" )
    @PreAuthorize ( "hasRole('ROLE_ADVOCATE')" )
    public String index ( final Model model ) {
        return Role.ROLE_ADVOCATE.getLanding();
    }

    /**
     * Provides the page for a User to view and edit their demographics
     *
     * @param model
     *            The data for the front end
     * @return The page to show the user so they can edit demographics
     */
    @GetMapping ( value = "advocate/editDemographics" )
    @PreAuthorize ( "hasRole('ROLE_ADVOCATE')" )
    public String viewDemographics ( final Model model ) {
        return "/advocate/editDemographics";
    }

    /**
     * Provides the page for an Advocate to view Patient appointment requests
     *
     * @param model
     *            The data for the front end
     * @return The page to show the user so they can view appointment requests
     */
    @GetMapping ( value = "advocate/viewAppointmentRequests" )
    @PreAuthorize ( "hasRole('ROLE_ADVOCATE')" )
    public String viewAppointmentRequests ( final Model model ) {
        return "/advocate/viewAppointmentRequests";
    }
  
    /**
     * Provides the page for a User to view their associated Patient's data
     *
     * @param model
     *            The data for the front end
     * @return The page to show the user to view data
     */
    @GetMapping ( value = "advocate/viewPatientData" )
    @PreAuthorize ( "hasRole('ROLE_ADVOCATE')" )
    public String viewPatientData ( final Model model ) {
        return "/advocate/viewPatientData";
    }
}
