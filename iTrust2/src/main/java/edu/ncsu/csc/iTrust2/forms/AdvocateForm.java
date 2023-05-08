package edu.ncsu.csc.iTrust2.forms;

import javax.validation.constraints.NotEmpty;

import org.hibernate.validator.constraints.Length;

/**
 * Form for editing Patient Advocate personnel specifically.
 *
 * @author Kai Presler-Marshall
 *
 */
public class AdvocateForm extends PersonnelForm {

    /**
     * Middle name of the Personnel
     */
    @Length ( max = 20 )
    private String middleName;

    /**
     * Nickname of the Personnel
     */
    @Length ( max = 20 )
    private String nickname;

    /**
     * Password of the user
     */
    @NotEmpty
    @Length ( min = 6, max = 20 )
    private String password;

    /***
     * Confirmation password of the user
     */
    @NotEmpty
    @Length ( min = 6, max = 20 )
    private String password2;

    /**
     * Creates a AdvocateForm object. For initializing a blank form
     */
    public AdvocateForm () {

    }

    /**
     * Returns the middle name.
     *
     * @return the middle name.
     */
    public String getMiddleName () {
        return middleName;
    }

    /**
     * Sets the middle name
     *
     * @param middleName
     *            of the advocate
     */
    public void setMiddleName ( final String middleName ) {
        this.middleName = middleName;
    }

    /**
     * Returns nickname of the advocate
     *
     * @return nickname of the advocate
     */
    public String getNickname () {
        return nickname;
    }

    /**
     * Sets the nickname of the advocate
     *
     * @param nickname
     *            of the advocate
     */
    public void setNickname ( final String nickname ) {
        this.nickname = nickname;
    }

    /**
     * Gets the Password provided in the form
     *
     * @return Password provided
     */
    public String getPassword () {
        return password;
    }

    /**
     * Sets the Password for the User on the form.
     *
     * @param password
     *            Password of the user
     */
    public void setPassword ( final String password ) {
        this.password = password;
    }

    /**
     * Gets the Password provided in the form
     *
     * @return Password provided
     */
    public String getPassword2 () {
        return password2;
    }

    /**
     * Sets the Password for the User on the form.
     *
     * @param password
     *            Password of the user
     */
    public void setPassword2 ( final String password ) {
        this.password2 = password;
    }

}
