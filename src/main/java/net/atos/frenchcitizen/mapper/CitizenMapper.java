package net.atos.frenchcitizen.mapper;

import net.atos.frenchcitizen.model.Citizen;
import net.atos.frenchcitizen.model.CitizenCreationRequest;
import net.atos.frenchcitizen.model.CitizenResponse;
import net.atos.frenchcitizen.model.CitizenUpdateRequest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CitizenMapper {

    Citizen toCitizen(CitizenCreationRequest citizenCreationRequest);

    CitizenCreationRequest toCitizenCreationRequest(Citizen citizen);

    CitizenUpdateRequest toCitizenUpdateRequest(Citizen citizen);

    CitizenResponse toCitizenResponse(Citizen citizen);
}
