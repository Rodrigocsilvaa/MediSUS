package br.com.medisus.api.service;

import br.com.medisus.api.api.ApiDtos.*;
import br.com.medisus.api.api.ResourceNotFoundException;
import br.com.medisus.api.domain.Medication;
import br.com.medisus.api.repository.MedicationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MedicationService {
    private final MedicationRepository repository;
    public MedicationService(MedicationRepository repository) { this.repository = repository; }
    @Transactional(readOnly = true)
    public Page<MedicationResponse> list(Pageable pageable) {
        return repository.findAll(pageable).map(MedicationResponse::from);
    }
    @Transactional(readOnly = true)
    public MedicationResponse get(long id) { return MedicationResponse.from(find(id)); }
    @Transactional
    public MedicationResponse create(MedicationRequest request) {
        return MedicationResponse.from(repository.save(request.toEntity()));
    }
    @Transactional
    public MedicationResponse update(long id, MedicationRequest request) {
        Medication entity = find(id); request.apply(entity); return MedicationResponse.from(repository.save(entity));
    }
    @Transactional
    public void delete(long id) { repository.delete(find(id)); }
    private Medication find(long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Medication", id));
    }
}
