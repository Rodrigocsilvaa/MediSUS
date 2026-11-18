package br.com.medisus.api.api;

import br.com.medisus.api.domain.*;
import jakarta.validation.constraints.*;
import java.time.Instant;

public final class ApiDtos {
    private ApiDtos() {}

    public record RegisterRequest(@NotBlank @Size(max = 120) String name,
                                  @NotBlank @Email @Size(max = 180) String email,
                                  @NotBlank @Size(min = 8, max = 72) String password) {}
    public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {}
    public record TokenResponse(String token, String tokenType, long expiresIn, Role role, String name) {}
    public record RoleRequest(@NotNull Role role) {}

    public record UbsRequest(@NotBlank @Size(max = 120) String name,
                             @NotBlank @Size(max = 240) String address,
                             @NotBlank @Size(max = 100) String city,
                             @NotBlank @Pattern(regexp = "[A-Za-z]{2}") String state,
                             @DecimalMin("-90.0") @DecimalMax("90.0") Double latitude,
                             @DecimalMin("-180.0") @DecimalMax("180.0") Double longitude) {
        public Ubs toEntity() { return new Ubs(name, address, city, state.toUpperCase(java.util.Locale.ROOT), latitude, longitude); }
        public void apply(Ubs ubs) {
            ubs.setName(name); ubs.setAddress(address); ubs.setCity(city);
            ubs.setState(state.toUpperCase(java.util.Locale.ROOT)); ubs.setLatitude(latitude); ubs.setLongitude(longitude);
        }
    }
    public record UbsResponse(Long id, String name, String address, String city, String state,
                              Double latitude, Double longitude) {
        public static UbsResponse from(Ubs u) {
            return new UbsResponse(u.getId(), u.getName(), u.getAddress(), u.getCity(), u.getState(),
                    u.getLatitude(), u.getLongitude());
        }
    }
    public record MedicationRequest(@NotBlank @Size(max = 160) String name,
                                    @NotBlank @Size(max = 160) String activeIngredient,
                                    @Size(max = 500) String description) {
        public Medication toEntity() { return new Medication(name, activeIngredient, description); }
        public void apply(Medication m) {
            m.setName(name); m.setActiveIngredient(activeIngredient); m.setDescription(description);
        }
    }
    public record MedicationResponse(Long id, String name, String activeIngredient, String description) {
        public static MedicationResponse from(Medication m) {
            return new MedicationResponse(m.getId(), m.getName(), m.getActiveIngredient(), m.getDescription());
        }
    }
    public record StockRequest(@NotNull @Positive Long ubsId, @NotNull @Positive Long medicationId,
                              @NotNull @PositiveOrZero Integer quantity) {}
    public record StockResponse(Long id, Long ubsId, String ubsName, Long medicationId,
                                String medicationName, int quantity, Instant updatedAt,
                                String farmaciaPopularEligibility) {
        public static StockResponse from(Stock s) {
            return new StockResponse(s.getId(), s.getUbs().getId(), s.getUbs().getName(),
                    s.getMedication().getId(), s.getMedication().getName(), s.getQuantity(),
                    s.getUpdatedAt(), "UNKNOWN");
        }
    }
    public record AlertRequest(@NotNull @Positive Long medicationId, @Positive Long ubsId) {}
    public record AlertResponse(Long id, Long medicationId, String medicationName, Long ubsId,
                                boolean active, Instant createdAt) {
        public static AlertResponse from(StockAlert a) {
            return new AlertResponse(a.getId(), a.getMedication().getId(), a.getMedication().getName(),
                    a.getUbs() == null ? null : a.getUbs().getId(), a.isActive(), a.getCreatedAt());
        }
    }
    public record NotificationResponse(Long id, String message, boolean read, Instant createdAt) {
        public static NotificationResponse from(AppNotification n) {
            return new NotificationResponse(n.getId(), n.getMessage(), n.isRead(), n.getCreatedAt());
        }
    }
    public record UserResponse(Long id, String name, String email, Role role) {
        public static UserResponse from(AppUser u) { return new UserResponse(u.getId(), u.getName(), u.getEmail(), u.getRole()); }
    }
    public record PrescriptionCandidates(java.util.List<String> candidates, String instruction) {}
}
