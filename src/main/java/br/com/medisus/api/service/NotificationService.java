package br.com.medisus.api.service;

import br.com.medisus.api.api.ApiDtos.NotificationResponse;
import br.com.medisus.api.api.ResourceNotFoundException;
import br.com.medisus.api.domain.AppNotification;
import br.com.medisus.api.repository.AppNotificationRepository;
import br.com.medisus.api.repository.AppUserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    private final AppNotificationRepository notifications;
    private final AppUserRepository users;
    public NotificationService(AppNotificationRepository notifications, AppUserRepository users) {
        this.notifications = notifications; this.users = users;
    }
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Page<NotificationResponse> list(String email, Pageable pageable) {
        var user = users.findByEmailIgnoreCase(email).orElseThrow(() -> new ResourceNotFoundException("User", email));
        return notifications.findByUserId(user.getId(), pageable).map(NotificationResponse::from);
    }
    @org.springframework.transaction.annotation.Transactional
    public NotificationResponse markRead(String email, long id) {
        var user = users.findByEmailIgnoreCase(email).orElseThrow(() -> new ResourceNotFoundException("User", email));
        AppNotification n = notifications.findById(id).orElseThrow(() -> new ResourceNotFoundException("Notification", id));
        if (!n.getUser().getId().equals(user.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("Notification belongs to another user");
        }
        n.setRead(true);
        return NotificationResponse.from(notifications.save(n));
    }
}
