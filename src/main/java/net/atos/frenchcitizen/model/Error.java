package net.atos.frenchcitizen.model;

import lombok.Data;

@Data
public class Error {
    private String field;
    private String detail;
}
