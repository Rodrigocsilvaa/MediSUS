package br.com.medisus.api.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "stocks", uniqueConstraints = @UniqueConstraint(columnNames = {"ubs_id", "medication_id"}))
public class Stock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "ubs_id", nullable = false)
    private Ubs ubs;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "medication_id", nullable = false)
    private Medication medication;
    @Column(nullable = false)
    private int quantity;
    @Column(nullable = false)
    private Instant updatedAt;

    protected Stock() {}

    public Stock(Ubs ubs, Medication medication, int quantity) {
        this.ubs = ubs;
        this.medication = medication;
        this.quantity = quantity;
        this.updatedAt = Instant.now();
    }

    @PrePersist
    @PreUpdate
    void updateTimestamp() { updatedAt = Instant.now(); }

    public Long getId() { return id; }
    public Ubs getUbs() { return ubs; }
    public void setUbs(Ubs ubs) { this.ubs = ubs; }
    public Medication getMedication() { return medication; }
    public void setMedication(Medication medication) { this.medication = medication; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public Instant getUpdatedAt() { return updatedAt; }
}
