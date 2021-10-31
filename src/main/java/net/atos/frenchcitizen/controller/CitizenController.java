package net.atos.frenchcitizen.controller;

import net.atos.frenchcitizen.exception.ConflictException;
import net.atos.frenchcitizen.exception.NotFoundException;
import net.atos.frenchcitizen.mapper.CitizenMapper;
import net.atos.frenchcitizen.model.Citizen;
import net.atos.frenchcitizen.model.CitizenRequest;
import net.atos.frenchcitizen.service.CitizenService;
import net.atos.frenchcitizen.util.PasswordUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.util.Optional;

@RestController
public class CitizenController {

    private final CitizenService citizenService;
    private final CitizenMapper citizenMapper;
    @Value("${password.encryption.salt:default}")
    private String salt;

    public CitizenController(CitizenService citizenService, CitizenMapper citizenMapper) {
        this.citizenService = citizenService;
        this.citizenMapper = citizenMapper;
    }

    @PostMapping("/citizen")
    private ResponseEntity<String> createCitizen(@Valid @RequestBody CitizenRequest citizenRequest) {
        if (citizenService.existsByUsername(citizenRequest.getUsername())) {
            throw new ConflictException("username", "already exists");
        }
        String encryptedPassword = PasswordUtils.encrypt(citizenRequest.getPassword(), salt);
        citizenRequest.setPassword(encryptedPassword);
        Citizen citizen = citizenService.save(citizenMapper.toCitizen(citizenRequest));
        return ResponseEntity.created(URI.create("/citizen/" + citizen.getId())).build();
    }

    @GetMapping("/citizen/{id}")
    private ResponseEntity<Citizen> findCitizen(@PathVariable Long id) {
        Optional<Citizen> citizen = citizenService.findCitizenById(id);
        if (citizen.isEmpty()) {
            throw new NotFoundException(null, "No citizen founded");
        }
        return ResponseEntity.ok(citizen.get());
    }

    @PostMapping("/citizen/{id}")
    private ResponseEntity<Void> updateCitizen(@PathVariable Long id, @Valid @RequestBody CitizenRequest citizenRequest) {
        if (citizenService.findCitizenById(id).isEmpty()) {
            throw new NotFoundException(null, "This citizen does not exist");
        }
        Citizen citizen = citizenMapper.toCitizen(citizenRequest);
        citizen.setId(id);
        citizenService.save(citizen);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/citizen/{id}")
    private ResponseEntity<Void> deleteCitizen(@PathVariable Long id) {
        Optional<Citizen> citizen = citizenService.findCitizenById(id);
        if (citizen.isEmpty()) {
            throw new NotFoundException(null, "This citizen does not exist");
        }
        citizenService.delete(citizen.get());
        return ResponseEntity.noContent().build();
    }
}
