package net.atos.frenchcitizen.model;

import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
@ToString(onlyExplicitlyIncluded = true)
public class CitizenTokenRequest {

    @ToString.Include
    @NotEmpty
    public String username;

    @NotNull
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{8,}$",
            message = "Minimum eight characters, at least one uppercase letter, one lowercase letter, one number and no special character")
    public String password;
}
