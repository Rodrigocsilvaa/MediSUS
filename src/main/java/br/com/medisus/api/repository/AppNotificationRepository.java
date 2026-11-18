package br.com.medisus.api.repository;

import br.com.medisus.api.domain.AppNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AppNotificationRepository extends JpaRepository<AppNotification, Long> {
    Page<AppNotification> findByUserId(Long userId, Pageable pageable);
}
