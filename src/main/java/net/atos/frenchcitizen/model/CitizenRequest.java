package net.atos.frenchcitizen.model;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;

@Data
public class CitizenRequest {

    @NotEmpty
    public String username;

    @NotEmpty
    public String password;

    public String firstname;

    public String lastname;

    public String phoneNumber;

    @NotNull
    public LocalDate birthdate;

    @NotNull
    @Pattern(regexp = "^(France)$", message = "only french citizens can registered", flags = {Pattern.Flag.CASE_INSENSITIVE})
    public String residenceCountry;

    public Gender gender;

    public Citizen toCitizen() {
        Citizen citizen = new Citizen();
        citizen.setUsername(username);
        citizen.setPassword(password);
        citizen.setFirstname(firstname);
        citizen.setLastname(lastname);
        citizen.setBirthdate(birthdate);
        citizen.setResidenceCountry(residenceCountry);
        citizen.setGender(gender);

        return citizen;
    }
}
