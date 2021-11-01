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
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{8,}$",
            message = "Minimum eight characters, at least one uppercase letter, one lowercase letter, one number and no special character")
    public String password;

    public String firstname;

    public String lastname;

    public String phoneNumber;

    @NotNull
    public LocalDate birthdate;

    @NotNull
    public String residenceCountry;

    public Gender gender;

    public boolean isFrenchCitizen() {
        java.util.regex.Pattern frenchPattern = java.util.regex.Pattern.compile("^(France)$", java.util.regex.Pattern.CASE_INSENSITIVE);
        return residenceCountry != null && frenchPattern.matcher(residenceCountry).matches();
    }

    public boolean isAdult() {
        return birthdate != null && birthdate.isBefore(LocalDate.now().minusYears(18));
    }
}
