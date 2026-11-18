package br.com.medisus.api.api;

import br.com.medisus.api.api.ApiDtos.*;
import br.com.medisus.api.service.MedicationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/medications")
public class MedicationController {
    private final MedicationService service;
    public MedicationController(MedicationService service) { this.service = service; }
    @GetMapping public Page<MedicationResponse> list(Pageable pageable) { return service.list(pageable); }
    @GetMapping("/{id}") public MedicationResponse get(@PathVariable long id) { return service.get(id); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('PHARMACIST','MANAGER')")
    public MedicationResponse create(@Valid @RequestBody MedicationRequest request) { return service.create(request); }
    @PutMapping("/{id}") @PreAuthorize("hasAnyRole('PHARMACIST','MANAGER')")
    public MedicationResponse update(@PathVariable long id, @Valid @RequestBody MedicationRequest request) {
        return service.update(id, request);
    }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('PHARMACIST','MANAGER')")
    public void delete(@PathVariable long id) { service.delete(id); }
}
