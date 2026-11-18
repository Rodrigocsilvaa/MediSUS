package br.com.medisus.api.repository;

import br.com.medisus.api.domain.StockAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface StockAlertRepository extends JpaRepository<StockAlert, Long> {
    List<StockAlert> findByMedicationIdAndActiveTrue(Long medicationId);
    Page<StockAlert> findByUserIdAndActiveTrue(Long userId, Pageable pageable);
}
