package net.atos.frenchcitizen.controller;

import net.atos.frenchcitizen.exception.ConflictException;
import net.atos.frenchcitizen.exception.ForbiddenException;
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
    @Value("${encryption.password.key}")
    private String salt;

    public CitizenController(CitizenService citizenService, CitizenMapper citizenMapper) {
        this.citizenService = citizenService;
        this.citizenMapper = citizenMapper;
    }

    @PostMapping("/citizen")
    public ResponseEntity<String> createCitizen(@Valid @RequestBody CitizenRequest citizenRequest) {
        if (!citizenRequest.isAdult()) {
            throw new ForbiddenException("birthdate", "Only adults can register");
        }
        if (!citizenRequest.isFrenchCitizen()) {
            throw new ForbiddenException("residenceCountry", "Only french citizens can register");
        }
        if (citizenService.existsByUsername(citizenRequest.getUsername())) {
            throw new ConflictException("username", "already exists");
        }
        String encryptedPassword = PasswordUtils.encrypt(citizenRequest.getPassword(), salt);
        citizenRequest.setPassword(encryptedPassword);
        Citizen citizen = citizenService.save(citizenMapper.toCitizen(citizenRequest));
        return ResponseEntity.created(URI.create("/citizen/" + citizen.getId())).build();
    }

    @GetMapping("/citizen/{id}")
    public ResponseEntity<Citizen> findCitizen(@PathVariable Long id) {
        Optional<Citizen> citizen = citizenService.findCitizenById(id);
        if (citizen.isEmpty()) {
            throw new NotFoundException(null, "No citizen founded");
        }
        String decryptedPassword = PasswordUtils.decrypt(citizen.get().getPassword(), salt);
        citizen.get().setPassword(decryptedPassword);
        return ResponseEntity.ok(citizen.get());
    }

    @PostMapping("/citizen/{id}")
    public ResponseEntity<Void> updateCitizen(@PathVariable Long id, @Valid @RequestBody CitizenRequest citizenRequest) {
        Optional<Citizen> previousCitizen = citizenService.findCitizenById(id);
        if (previousCitizen.isEmpty()) {
            throw new NotFoundException(null, "This citizen does not exist");
        }
        if (citizenService.existsByUsername(citizenRequest.getUsername()) && !previousCitizen.get().getUsername().equals(citizenRequest.getUsername())) {
            throw new ConflictException("username", "already exists");
        }
        String encryptedPassword = PasswordUtils.encrypt(citizenRequest.getPassword(), salt);
        citizenRequest.setPassword(encryptedPassword);
        Citizen citizen = citizenMapper.toCitizen(citizenRequest);
        citizen.setId(id);
        citizenService.save(citizen);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/citizen/{id}")
    public ResponseEntity<Void> deleteCitizen(@PathVariable Long id) {
        Optional<Citizen> citizen = citizenService.findCitizenById(id);
        if (citizen.isEmpty()) {
            throw new NotFoundException(null, "This citizen does not exist");
        }
        citizenService.delete(citizen.get());
        return ResponseEntity.noContent().build();
    }
}
