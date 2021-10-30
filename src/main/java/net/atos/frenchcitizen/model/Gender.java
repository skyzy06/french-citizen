package net.atos.frenchcitizen.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum Gender {
    M("Male"),
    F("Female"),
    U("Unknown");

    @Getter
    public String value;
}
