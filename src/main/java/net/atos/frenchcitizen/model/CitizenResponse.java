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
    private String username;

    private String firstname;

    private String lastname;

    private String phoneNumber;

    @NotNull
    private LocalDate birthdate;

    @NotNull
    private String residenceCountry;

    private Gender gender;
}
