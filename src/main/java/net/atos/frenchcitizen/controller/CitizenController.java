package net.atos.frenchcitizen.controller;

import net.atos.frenchcitizen.model.Citizen;
import net.atos.frenchcitizen.service.CitizenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class CitizenController {

    @Autowired
    CitizenService citizenService;

    @PostMapping("/citizen")
    private Citizen createCitizen(@RequestBody Citizen citizen) {
        return citizenService.save(citizen);
    }

    @GetMapping("/citizen/{id}")
    private Citizen findCitizen(@PathVariable Long id) {
        return citizenService.findCitizenById(id).orElse(null);
    }

    @DeleteMapping("/citizen/{id}")
    private void deleteCitizen(@PathVariable Long id) {
        Citizen citizen = findCitizen(id);
        if (citizen != null) {
            citizenService.delete(citizen);
        }
    }
}
