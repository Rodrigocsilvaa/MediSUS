package br.com.medisus.api.service;

import br.com.medisus.api.api.ApiDtos.*;
import br.com.medisus.api.api.ResourceNotFoundException;
import br.com.medisus.api.domain.*;
import br.com.medisus.api.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class AlertService {
    private final StockAlertRepository alerts;
    private final AppUserRepository users;
    private final MedicationRepository medications;
    private final UbsRepository clinics;

    public AlertService(StockAlertRepository alerts, AppUserRepository users,
                        MedicationRepository medications, UbsRepository clinics) {
        this.alerts = alerts; this.users = users; this.medications = medications; this.clinics = clinics;
    }

    @org.springframework.transaction.annotation.Transactional
    public AlertResponse create(String email, AlertRequest request) {
        AppUser user = users.findByEmailIgnoreCase(email).orElseThrow(() -> new ResourceNotFoundException("User", email));
        Medication medication = medications.findById(request.medicationId())
                .orElseThrow(() -> new ResourceNotFoundException("Medication", request.medicationId()));
        Ubs clinic = request.ubsId() == null ? null : clinics.findById(request.ubsId())
                .orElseThrow(() -> new ResourceNotFoundException("UBS", request.ubsId()));
        return AlertResponse.from(alerts.save(new StockAlert(user, medication, clinic)));
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Page<AlertResponse> list(String email, Pageable pageable) {
        AppUser user = users.findByEmailIgnoreCase(email).orElseThrow(() -> new ResourceNotFoundException("User", email));
        return alerts.findByUserIdAndActiveTrue(user.getId(), pageable).map(AlertResponse::from);
    }

    @org.springframework.transaction.annotation.Transactional
    public void deactivate(String email, long alertId) {
        StockAlert alert = alerts.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert", alertId));
        if (!alert.getUser().getEmail().equalsIgnoreCase(email)) {
            throw new org.springframework.security.access.AccessDeniedException("Alert belongs to another user");
        }
        alert.setActive(false);
        alerts.save(alert);
    }
}
