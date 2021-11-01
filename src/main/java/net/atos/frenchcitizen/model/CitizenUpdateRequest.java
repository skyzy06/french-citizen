package net.atos.frenchcitizen.model;

import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotEmpty;

@Data
@ToString(onlyExplicitlyIncluded = true)
public class CitizenUpdateRequest {

    @ToString.Include
    @NotEmpty
    public String username;

    public String firstname;

    public String lastname;

    public String phoneNumber;

    public Gender gender;
}
