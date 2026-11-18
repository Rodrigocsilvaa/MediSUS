package br.com.medisus.api.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "ubs")
public class Ubs {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 120)
    private String name;
    @Column(nullable = false, length = 240)
    private String address;
    @Column(nullable = false, length = 100)
    private String city;
    @Column(nullable = false, length = 2)
    private String state;
    private Double latitude;
    private Double longitude;

    protected Ubs() {}

    public Ubs(String name, String address, String city, String state, Double latitude, Double longitude) {
        this.name = name;
        this.address = address;
        this.city = city;
        this.state = state;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
}
