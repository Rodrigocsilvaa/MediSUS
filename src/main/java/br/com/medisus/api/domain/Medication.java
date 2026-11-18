package br.com.medisus.api.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "medications")
public class Medication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 160)
    private String name;
    @Column(nullable = false, length = 160)
    private String activeIngredient;
    @Column(length = 500)
    private String description;

    protected Medication() {}

    public Medication(String name, String activeIngredient, String description) {
        this.name = name;
        this.activeIngredient = activeIngredient;
        this.description = description;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getActiveIngredient() { return activeIngredient; }
    public void setActiveIngredient(String activeIngredient) { this.activeIngredient = activeIngredient; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
