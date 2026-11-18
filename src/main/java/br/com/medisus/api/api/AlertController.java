package br.com.medisus.api.api;

import br.com.medisus.api.api.ApiDtos.*;
import br.com.medisus.api.domain.AppUser;
import br.com.medisus.api.service.AlertService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/alerts")
@PreAuthorize("hasRole('CITIZEN')")
public class AlertController {
    private final AlertService service;
    public AlertController(AlertService service) { this.service = service; }
    @GetMapping public Page<AlertResponse> list(@AuthenticationPrincipal AppUser user, Pageable pageable) {
        return service.list(user.getEmail(), pageable);
    }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public AlertResponse create(@AuthenticationPrincipal AppUser user, @Valid @RequestBody AlertRequest request) {
        return service.create(user.getEmail(), request);
    }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal AppUser user, @PathVariable long id) {
        service.deactivate(user.getEmail(), id);
    }
}
