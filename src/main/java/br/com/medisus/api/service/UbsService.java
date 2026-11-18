package br.com.medisus.api.service;

import br.com.medisus.api.api.ApiDtos.*;
import br.com.medisus.api.api.ResourceNotFoundException;
import br.com.medisus.api.domain.Ubs;
import br.com.medisus.api.repository.UbsRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UbsService {
    private final UbsRepository repository;
    public UbsService(UbsRepository repository) { this.repository = repository; }
    @Transactional(readOnly = true)
    public Page<UbsResponse> list(Pageable pageable) { return repository.findAll(pageable).map(UbsResponse::from); }
    @Transactional(readOnly = true)
    public UbsResponse get(long id) { return UbsResponse.from(find(id)); }
    @Transactional
    public UbsResponse create(UbsRequest request) { return UbsResponse.from(repository.save(request.toEntity())); }
    @Transactional
    public UbsResponse update(long id, UbsRequest request) {
        Ubs entity = find(id); request.apply(entity); return UbsResponse.from(repository.save(entity));
    }
    @Transactional
    public void delete(long id) { repository.delete(find(id)); }
    private Ubs find(long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("UBS", id));
    }
}
