package net.atos.frenchcitizen.controller;

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
        String encryptedPassword = PasswordUtils.encrypt(citizenRequest.getPassword(), salt);
        citizenRequest.setPassword(encryptedPassword);
        Citizen citizen = citizenService.save(citizenMapper.toCitizen(citizenRequest));
        return ResponseEntity.created(URI.create("/citizen/" + citizen.getId())).build();
    }

    @GetMapping("/citizen/{id}")
    private ResponseEntity<Citizen> findCitizen(@PathVariable Long id) {
        Optional<Citizen> citizen = citizenService.findCitizenById(id);
        return citizen.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/citizen/{id}")
    private ResponseEntity<Void> updateCitizen(@PathVariable Long id, @Valid @RequestBody CitizenRequest citizenRequest) {
        Citizen citizen = citizenMapper.toCitizen(citizenRequest);
        citizen.setId(id);
        citizenService.save(citizen);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/citizen/{id}")
    private ResponseEntity<Void> deleteCitizen(@PathVariable Long id) {
        citizenService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
