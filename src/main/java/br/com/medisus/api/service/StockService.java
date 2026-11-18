package br.com.medisus.api.service;

import br.com.medisus.api.api.ApiDtos.*;
import br.com.medisus.api.api.ResourceNotFoundException;
import br.com.medisus.api.api.ConflictException;
import br.com.medisus.api.domain.*;
import br.com.medisus.api.messaging.RabbitConfiguration;
import br.com.medisus.api.messaging.StockReplenishedEvent;
import br.com.medisus.api.repository.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StockService {
    private final StockRepository stocks;
    private final UbsRepository ubs;
    private final MedicationRepository medications;
    private final RabbitTemplate rabbit;

    public StockService(StockRepository stocks, UbsRepository ubs, MedicationRepository medications,
                        RabbitTemplate rabbit) {
        this.stocks = stocks; this.ubs = ubs; this.medications = medications; this.rabbit = rabbit;
    }

    @Transactional(readOnly = true)
    public Page<StockResponse> list(Long medicationId, Long ubsId, String medicationName, Pageable pageable) {
        return stocks.findAll(StockSpecification.available(medicationId, ubsId, medicationName), pageable)
                .map(StockResponse::from);
    }

    @Transactional(readOnly = true)
    public StockResponse get(long id) { return StockResponse.from(find(id)); }

    @Transactional
    public StockResponse upsert(StockRequest request) {
        Ubs clinic = ubs.findById(request.ubsId()).orElseThrow(() -> new ResourceNotFoundException("UBS", request.ubsId()));
        Medication medication = medications.findById(request.medicationId())
                .orElseThrow(() -> new ResourceNotFoundException("Medication", request.medicationId()));
        Stock stock = stocks.findByUbsIdAndMedicationId(clinic.getId(), medication.getId())
                .orElseGet(() -> new Stock(clinic, medication, 0));
        return saveQuantity(stock, request.quantity());
    }

    @Transactional
    public StockResponse update(long id, StockRequest request) {
        Stock stock = find(id);
        if (!stock.getUbs().getId().equals(request.ubsId())
                || !stock.getMedication().getId().equals(request.medicationId())) {
            throw new ConflictException("A stock record cannot be moved to another UBS or medication");
        }
        return saveQuantity(stock, request.quantity());
    }

    private StockResponse saveQuantity(Stock stock, int quantity) {
        boolean replenished = quantity > stock.getQuantity();
        stock.setQuantity(quantity);
        Stock saved = stocks.save(stock);
        if (replenished) {
            rabbit.convertAndSend(RabbitConfiguration.STOCK_REPLENISHED_QUEUE,
                    new StockReplenishedEvent(saved.getId(), saved.getMedication().getId(),
                            saved.getMedication().getName(), saved.getUbs().getId(), saved.getUbs().getName()));
        }
        return StockResponse.from(saved);
    }

    @Transactional
    public void delete(long id) { stocks.delete(find(id)); }

    private Stock find(long id) {
        return stocks.findById(id).orElseThrow(() -> new ResourceNotFoundException("Stock", id));
    }
}
