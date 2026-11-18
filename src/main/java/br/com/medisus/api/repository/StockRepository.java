package br.com.medisus.api.repository;

import br.com.medisus.api.domain.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface StockRepository extends JpaRepository<Stock, Long>, JpaSpecificationExecutor<Stock> {
    Optional<Stock> findByUbsIdAndMedicationId(Long ubsId, Long medicationId);
}
