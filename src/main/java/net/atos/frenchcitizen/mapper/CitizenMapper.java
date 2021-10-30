package net.atos.frenchcitizen.mapper;

import net.atos.frenchcitizen.model.Citizen;
import net.atos.frenchcitizen.model.CitizenRequest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CitizenMapper {

    Citizen toCitizen(CitizenRequest citizenRequest);
}
