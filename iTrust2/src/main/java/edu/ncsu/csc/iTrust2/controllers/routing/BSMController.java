package edu.ncsu.csc.iTrust2.controllers.routing;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import edu.ncsu.csc.iTrust2.models.enums.Role;

/**
 * Controller class responsible for managing the behavior for the BSM Landing
 * Screen
 *
 * @author nhwiblit
 * @author Alex Taylor (aktaylo4)
 *
 */
@Controller
public class BSMController {

    /**
     * Returns the Landing screen for the BSM
     *
     * @param model
     *            Data from the front end
     * @return The page to display
     */
    @RequestMapping ( value = "bsm/index" )
    @PreAuthorize ( "hasAnyRole('ROLE_BSM')" )
    public String index ( final Model model ) {
        return Role.ROLE_BSM.getLanding();
    }

    /**
     * Returns the page allowing BSMs to view/edit CPT codes
     *
     * @return The page to display
     */
    @GetMapping ( "/bsm/viewCPTCodes" )
    @PreAuthorize ( "hasAnyRole('ROLE_BSM')" )
    public String viewCPTCodes () {
        return "/bsm/viewCPTCodes";
    }

    /**
     * Returns the page allowing BSMs to manage bills
     *
     * @return The page to display
     */
    @GetMapping ( "/bsm/manageBills" )
    @PreAuthorize ( "hasAnyRole('ROLE_BSM')" )
    public String manageBills () {
        return "/bsm/manageBills";
    }

    /**
     * Provides the page for a User to view and edit their demographics
     *
     * @param model
     *            The data for the front end
     * @return The page to show the user so they can edit demographics
     */
    @GetMapping ( value = "bsm/editDemographics" )
    @PreAuthorize ( "hasRole('ROLE_BSM')" )
    public String viewDemographics ( final Model model ) {
        return "/bsm/editDemographics";
    }

}
