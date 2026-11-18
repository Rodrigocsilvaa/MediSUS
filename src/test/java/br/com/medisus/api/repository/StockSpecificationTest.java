package br.com.medisus.api.repository;

import br.com.medisus.api.domain.Medication;
import br.com.medisus.api.domain.Stock;
import br.com.medisus.api.domain.Ubs;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class StockSpecificationTest {
    @Autowired private StockRepository stocks;
    @Autowired private UbsRepository ubs;
    @Autowired private MedicationRepository medications;

    @Test
    void availabilitySearchUsesPersistedPositiveStockAndOptionalFilters() {
        Ubs clinic = ubs.save(new Ubs("Central", "Rua A", "São Paulo", "SP", null, null));
        Ubs otherClinic = ubs.save(new Ubs("North", "Rua B", "São Paulo", "SP", null, null));
        Medication medicine = medications.save(new Medication("Paracetamol 500 mg", "paracetamol", null));
        Medication otherMedicine = medications.save(new Medication("Dipirona", "dipirona", null));
        stocks.save(new Stock(clinic, medicine, 5));
        stocks.save(new Stock(otherClinic, medicine, 0));
        stocks.save(new Stock(otherClinic, otherMedicine, 9));

        var result = stocks.findAll(
                StockSpecification.available(medicine.getId(), clinic.getId(), "para"),
                PageRequest.of(0, 20));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).extracting(Stock::getQuantity).containsExactly(5);
    }
}
