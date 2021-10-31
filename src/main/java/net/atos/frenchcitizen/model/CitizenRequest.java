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

    @NotNull
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{8,}$", message = "Minimum eight characters, at least one uppercase letter, one lowercase letter and one number")
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
}
