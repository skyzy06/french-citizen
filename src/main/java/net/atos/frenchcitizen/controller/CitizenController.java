package net.atos.frenchcitizen.controller;

import net.atos.frenchcitizen.aop.security.Secured;
import net.atos.frenchcitizen.exception.BadRequestException;
import net.atos.frenchcitizen.exception.ConflictException;
import net.atos.frenchcitizen.exception.ForbiddenException;
import net.atos.frenchcitizen.exception.NotFoundException;
import net.atos.frenchcitizen.mapper.CitizenMapper;
import net.atos.frenchcitizen.model.*;
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
    public ResponseEntity<String> createCitizen(@Valid @RequestBody CitizenCreationRequest requestBody) {
        if (!requestBody.isAdult()) {
            throw new ForbiddenException("birthdate", "Only adults can register");
        }
        if (!requestBody.isFrenchCitizen()) {
            throw new ForbiddenException("residenceCountry", "Only french citizens can register");
        }
        if (citizenService.existsByUsername(requestBody.getUsername())) {
            throw new ConflictException("username", "already exists");
        }
        String encryptedPassword = PasswordUtils.encrypt(requestBody.getPassword(), salt);
        requestBody.setPassword(encryptedPassword);
        Citizen citizen = citizenService.save(citizenMapper.toCitizen(requestBody));
        return ResponseEntity.created(URI.create("/citizen/" + citizen.getId())).build();
    }

    @Secured
    @GetMapping("/citizen/{id}")
    public ResponseEntity<CitizenResponse> findCitizen(@PathVariable Long id) {
        Citizen citizen = citizenService.findCitizenById(id).orElseThrow(() -> new NotFoundException(null, "No citizen founded"));
        return ResponseEntity.ok(citizenMapper.toCitizenResponse(citizen));
    }

    @Secured
    @PostMapping("/citizen/{id}")
    public ResponseEntity<Void> updateCitizen(@PathVariable Long id, @Valid @RequestBody CitizenUpdateRequest requestBody) {
        Citizen citizen = citizenService.findCitizenById(id).orElseThrow(() -> new NotFoundException(null, "This citizen does not exist"));
        if (citizenService.existsByUsername(requestBody.getUsername()) && !citizen.getUsername().equals(requestBody.getUsername())) {
            throw new ConflictException("username", "already exists");
        }

        citizen.setUsername(requestBody.getUsername());
        citizen.setFirstname(requestBody.getFirstname());
        citizen.setLastname(requestBody.getLastname());
        citizen.setPhoneNumber(requestBody.getPhoneNumber());
        citizen.setGender(requestBody.getGender());

        citizenService.save(citizen);
        return ResponseEntity.noContent().build();
    }

    @Secured
    @PatchMapping("/citizen/{id}")
    public ResponseEntity<Void> updateCitizenPassword(@PathVariable Long id, @Valid @RequestBody CitizenPasswordUpdateRequest requestBody) {
        if (requestBody.getOldPassword().equals(requestBody.password)) {
            throw new BadRequestException("password", "identical to oldPassword");
        }
        Citizen citizen = citizenService.findCitizenById(id).orElseThrow(() -> new NotFoundException(null, "This citizen does not exist"));

        String savedPassword = PasswordUtils.decrypt(citizen.getPassword(), salt);
        if (!requestBody.getOldPassword().equals(savedPassword)) {
            throw new BadRequestException("oldPassword", "wrong value");
        }

        String encryptedPassword = PasswordUtils.encrypt(requestBody.getPassword(), salt);
        citizen.setPassword(encryptedPassword);
        citizenService.save(citizen);
        return ResponseEntity.noContent().build();
    }

    @Secured
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
