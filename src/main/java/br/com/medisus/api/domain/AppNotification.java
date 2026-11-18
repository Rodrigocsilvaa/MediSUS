package br.com.medisus.api.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "app_notifications")
public class AppNotification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;
    @Column(nullable = false, length = 500)
    private String message;
    @Column(name = "is_read", nullable = false)
    private boolean read;
    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    protected AppNotification() {}

    public AppNotification(AppUser user, String message) {
        this.user = user;
        this.message = message;
    }

    public Long getId() { return id; }
    public AppUser getUser() { return user; }
    public String getMessage() { return message; }
    public boolean isRead() { return read; }
    public Instant getCreatedAt() { return createdAt; }
    public void setRead(boolean read) { this.read = read; }
}
