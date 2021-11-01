package net.atos.frenchcitizen.model;

import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
@ToString(onlyExplicitlyIncluded = true)
public class CitizenResponse {

    @ToString.Include
    @NotEmpty
    public String username;

    public String firstname;

    public String lastname;

    public String phoneNumber;

    @NotNull
    public LocalDate birthdate;

    @NotNull
    public String residenceCountry;

    public Gender gender;
}
