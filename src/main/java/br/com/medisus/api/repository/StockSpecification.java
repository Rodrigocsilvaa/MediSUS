package br.com.medisus.api.repository;

import br.com.medisus.api.domain.Stock;
import org.springframework.data.jpa.domain.Specification;

public final class StockSpecification {
    private StockSpecification() {}

    public static Specification<Stock> available(Long medicationId, Long ubsId, String medicationName) {
        Specification<Stock> spec = (root, query, cb) -> cb.greaterThan(root.get("quantity"), 0);
        if (medicationId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("medication").get("id"), medicationId));
        }
        if (ubsId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("ubs").get("id"), ubsId));
        }
        if (medicationName != null && !medicationName.isBlank()) {
            String pattern = "%" + medicationName.trim().toLowerCase(java.util.Locale.ROOT) + "%";
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("medication").<String>get("name")), pattern));
        }
        return spec;
    }
}
