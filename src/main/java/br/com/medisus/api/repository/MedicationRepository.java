package br.com.medisus.api.repository;

import br.com.medisus.api.domain.Medication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MedicationRepository extends JpaRepository<Medication, Long> {
    Optional<Medication> findFirstByNameIgnoreCase(String name);
}
