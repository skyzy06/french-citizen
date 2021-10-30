package net.atos.frenchcitizen.service;

import net.atos.frenchcitizen.model.Citizen;
import net.atos.frenchcitizen.repository.CitizenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CitizenService {

    @Autowired
    public CitizenRepository citizenRepository;

    public Optional<Citizen> findCitizenById(Long id) {
        return citizenRepository.findById(id);
    }

    public Citizen save(Citizen citizen) {
        return citizenRepository.save(citizen);
    }

    public void delete(Citizen citizen) {
        citizenRepository.delete(citizen);
    }
}
