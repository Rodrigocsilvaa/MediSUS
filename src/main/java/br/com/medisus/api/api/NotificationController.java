package br.com.medisus.api.api;

import br.com.medisus.api.api.ApiDtos.NotificationResponse;
import br.com.medisus.api.domain.AppUser;
import br.com.medisus.api.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService service;
    public NotificationController(NotificationService service) { this.service = service; }
    @GetMapping public Page<NotificationResponse> list(@AuthenticationPrincipal AppUser user, Pageable pageable) {
        return service.list(user.getEmail(), pageable);
    }
    @PatchMapping("/{id}/read") public NotificationResponse markRead(@AuthenticationPrincipal AppUser user,
                                                                     @PathVariable long id) {
        return service.markRead(user.getEmail(), id);
    }
}
