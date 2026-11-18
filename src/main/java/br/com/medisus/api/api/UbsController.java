package br.com.medisus.api.api;

import br.com.medisus.api.api.ApiDtos.*;
import br.com.medisus.api.service.UbsService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ubs")
public class UbsController {
    private final UbsService service;
    public UbsController(UbsService service) { this.service = service; }
    @GetMapping public Page<UbsResponse> list(Pageable pageable) { return service.list(pageable); }
    @GetMapping("/{id}") public UbsResponse get(@PathVariable long id) { return service.get(id); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('PHARMACIST','MANAGER')")
    public UbsResponse create(@Valid @RequestBody UbsRequest request) { return service.create(request); }
    @PutMapping("/{id}") @PreAuthorize("hasAnyRole('PHARMACIST','MANAGER')")
    public UbsResponse update(@PathVariable long id, @Valid @RequestBody UbsRequest request) {
        return service.update(id, request);
    }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('PHARMACIST','MANAGER')")
    public void delete(@PathVariable long id) { service.delete(id); }
}
