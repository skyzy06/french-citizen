package net.atos.frenchcitizen.controller;

import io.swagger.annotations.Api;
import net.atos.frenchcitizen.aop.security.Secured;
import net.atos.frenchcitizen.exception.BadRequestException;
import net.atos.frenchcitizen.exception.ConflictException;
import net.atos.frenchcitizen.exception.ForbiddenException;
import net.atos.frenchcitizen.exception.NotFoundException;
import net.atos.frenchcitizen.helper.TokenHelper;
import net.atos.frenchcitizen.mapper.CitizenMapper;
import net.atos.frenchcitizen.model.*;
import net.atos.frenchcitizen.service.CitizenService;
import net.atos.frenchcitizen.service.UserDetailsImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.util.Optional;

import static net.atos.frenchcitizen.controller.ControllerMsgConstants.*;

@RestController
@Api(value = "Citizen API", tags = {"Citizen"})
public class CitizenController {

    private final CitizenService citizenService;
    private final CitizenMapper citizenMapper;
    private final TokenHelper tokenHelper;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    public CitizenController(CitizenService citizenService, CitizenMapper citizenMapper,
                             TokenHelper tokenHelper, PasswordEncoder passwordEncoder,
                             AuthenticationManager authenticationManager) {
        this.citizenService = citizenService;
        this.citizenMapper = citizenMapper;
        this.tokenHelper = tokenHelper;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/citizen")
    public ResponseEntity<String> createCitizen(@Valid @RequestBody CitizenCreationRequest requestBody) {
        if (!requestBody.isAdult()) {
            throw new ForbiddenException("birthdate", ONLY_ADULTS);
        }
        if (!requestBody.isFrenchCitizen()) {
            throw new ForbiddenException("residenceCountry", ONLY_FRENCH_CITIZENS);
        }
        if (citizenService.existsByUsername(requestBody.getUsername())) {
            throw new ConflictException("username", USERNAME_ALREADY_EXISTS);
        }
        String encryptedPassword = passwordEncoder.encode(requestBody.getPassword());
        requestBody.setPassword(encryptedPassword);
        Citizen citizen = citizenService.save(citizenMapper.toCitizen(requestBody));
        return ResponseEntity.created(URI.create("/citizen/" + citizen.getId())).build();
    }

    @PostMapping("/citizen/token")
    public ResponseEntity<String> requestToken(@Valid @RequestBody CitizenTokenRequest requestBody) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(requestBody.getUsername(), requestBody.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            String token = tokenHelper.encode(userDetails.getId());
            return ResponseEntity.ok(token);
        } catch (Exception e) {
            throw new BadRequestException(null, BAD_CREDENTIALS);
        }
    }

    @Secured
    @GetMapping("/citizen/{id}")
    public ResponseEntity<CitizenResponse> findCitizen(@PathVariable Long id) {
        Citizen citizen = citizenService.findCitizenById(id).orElseThrow(() -> new NotFoundException(null, UNKNOW_CITIZEN));
        return ResponseEntity.ok(citizenMapper.toCitizenResponse(citizen));
    }

    @Secured
    @PatchMapping("/citizen/{id}")
    public ResponseEntity<Void> updateCitizen(@PathVariable Long id, @Valid @RequestBody CitizenUpdateRequest requestBody) {
        Citizen citizen = citizenService.findCitizenById(id).orElseThrow(() -> new NotFoundException(null, UNKNOW_CITIZEN));
        if (citizenService.existsByUsername(requestBody.getUsername()) && !citizen.getUsername().equals(requestBody.getUsername())) {
            throw new ConflictException("username", USERNAME_ALREADY_EXISTS);
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
    @PatchMapping("/citizen/{id}/password")
    public ResponseEntity<Void> updateCitizenPassword(@PathVariable Long id, @Valid @RequestBody CitizenPasswordUpdateRequest requestBody) {
        if (requestBody.getOldPassword().equals(requestBody.getPassword())) {
            throw new BadRequestException("password", NEW_PASSWORD_IDENTICAL);
        }
        Citizen citizen = citizenService.findCitizenById(id).orElseThrow(() -> new NotFoundException(null, UNKNOW_CITIZEN));

        if (!passwordEncoder.matches(requestBody.getOldPassword(), citizen.getPassword())) {
            throw new BadRequestException("oldPassword", BAD_OLD_PASSWORD);
        }

        String encodedPassword = passwordEncoder.encode(requestBody.getPassword());
        citizen.setPassword(encodedPassword);
        citizenService.save(citizen);
        return ResponseEntity.noContent().build();
    }

    @Secured
    @DeleteMapping("/citizen/{id}")
    public ResponseEntity<Void> deleteCitizen(@PathVariable Long id) {
        Optional<Citizen> citizen = citizenService.findCitizenById(id);
        if (citizen.isEmpty()) {
            throw new NotFoundException(null, UNKNOW_CITIZEN);
        }
        citizenService.delete(citizen.get());
        return ResponseEntity.noContent().build();
    }
}
