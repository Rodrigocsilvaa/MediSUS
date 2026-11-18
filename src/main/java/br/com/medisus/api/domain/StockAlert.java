package br.com.medisus.api.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "stock_alerts")
public class StockAlert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "medication_id", nullable = false)
    private Medication medication;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ubs_id")
    private Ubs ubs;
    @Column(nullable = false)
    private boolean active = true;
    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    protected StockAlert() {}

    public StockAlert(AppUser user, Medication medication, Ubs ubs) {
        this.user = user;
        this.medication = medication;
        this.ubs = ubs;
    }

    public Long getId() { return id; }
    public AppUser getUser() { return user; }
    public Medication getMedication() { return medication; }
    public Ubs getUbs() { return ubs; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public Instant getCreatedAt() { return createdAt; }
}
