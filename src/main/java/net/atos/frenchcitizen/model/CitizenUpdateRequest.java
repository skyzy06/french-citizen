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
    public String username;

    public String firstname;

    public String lastname;

    public String phoneNumber;

    public Gender gender;
}
