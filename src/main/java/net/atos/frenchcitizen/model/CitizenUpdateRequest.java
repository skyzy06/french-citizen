package net.atos.frenchcitizen.model;

import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Data
@ToString(onlyExplicitlyIncluded = true)
public class CitizenUpdateRequest {

    @ToString.Include
    @NotEmpty
    @Size(max = 32, message = "must not be more than 32 characters")
    private String username;

    private String firstname;

    private String lastname;

    private String phoneNumber;

    private Gender gender;
}
