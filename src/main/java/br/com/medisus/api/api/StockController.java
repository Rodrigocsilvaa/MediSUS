package br.com.medisus.api.api;

import br.com.medisus.api.api.ApiDtos.*;
import br.com.medisus.api.service.StockService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stocks")
public class StockController {
    private final StockService service;
    public StockController(StockService service) { this.service = service; }
    @GetMapping public Page<StockResponse> list(@RequestParam(required = false) Long medicationId,
                                               @RequestParam(required = false) Long ubsId,
                                               @RequestParam(required = false) String medicationName,
                                               Pageable pageable) {
        return service.list(medicationId, ubsId, medicationName, pageable);
    }
    @GetMapping("/{id}") public StockResponse get(@PathVariable long id) { return service.get(id); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('PHARMACIST','MANAGER')")
    public StockResponse create(@Valid @RequestBody StockRequest request) { return service.upsert(request); }
    @PutMapping("/{id}") @PreAuthorize("hasAnyRole('PHARMACIST','MANAGER')")
    public StockResponse update(@PathVariable long id, @Valid @RequestBody StockRequest request) {
        return service.update(id, request);
    }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('PHARMACIST','MANAGER')")
    public void delete(@PathVariable long id) { service.delete(id); }
}
