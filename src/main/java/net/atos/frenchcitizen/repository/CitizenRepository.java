package net.atos.frenchcitizen.repository;

import net.atos.frenchcitizen.model.Citizen;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CitizenRepository extends JpaRepository<Citizen, Long> {

    boolean existsByUsername(String username);
}
