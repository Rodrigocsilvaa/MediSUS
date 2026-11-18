package br.com.medisus.api.service;

import br.com.medisus.api.api.ApiDtos.StockRequest;
import br.com.medisus.api.domain.Medication;
import br.com.medisus.api.domain.Stock;
import br.com.medisus.api.domain.Ubs;
import br.com.medisus.api.messaging.RabbitConfiguration;
import br.com.medisus.api.messaging.StockReplenishedEvent;
import br.com.medisus.api.messaging.StockReplenishedListener;
import br.com.medisus.api.domain.AppUser;
import br.com.medisus.api.domain.Role;
import br.com.medisus.api.domain.StockAlert;
import br.com.medisus.api.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.mockito.ArgumentCaptor;
import java.util.List;
import java.util.Optional;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

class StockServiceTest {
    @Test
    void replenishmentPublishesAnEventWithInventoryIdentifiersAndNames() {
        StockRepository stocks = mock(StockRepository.class);
        UbsRepository ubs = mock(UbsRepository.class);
        MedicationRepository medications = mock(MedicationRepository.class);
        RabbitTemplate rabbit = mock(RabbitTemplate.class);
        Ubs clinic = new Ubs("Central", "Rua A", "São Paulo", "SP", null, null);
        Medication medication = new Medication("Paracetamol", "paracetamol", null);
        when(ubs.findById(1L)).thenReturn(Optional.of(clinic));
        when(medications.findById(2L)).thenReturn(Optional.of(medication));
        Stock existing = new Stock(clinic, medication, 2);
        when(stocks.findByUbsIdAndMedicationId(1L, 2L)).thenReturn(Optional.of(existing));
        when(stocks.save(any(Stock.class))).thenAnswer(call -> call.getArgument(0));

        new StockService(stocks, ubs, medications, rabbit).upsert(new StockRequest(1L, 2L, 8));

        ArgumentCaptor<StockReplenishedEvent> eventCaptor = ArgumentCaptor.forClass(StockReplenishedEvent.class);
        verify(rabbit).convertAndSend(eq(RabbitConfiguration.STOCK_REPLENISHED_QUEUE), eventCaptor.capture());
        assertThat(eventCaptor.getValue().medicationName()).isEqualTo("Paracetamol");
        assertThat(eventCaptor.getValue().ubsName()).isEqualTo("Central");
    }

    @Test
    void rabbitConsumerNotifiesOnlyAlertsMatchingTheReplenishedUbs() {
        StockAlertRepository alerts = mock(StockAlertRepository.class);
        AppNotificationRepository notifications = mock(AppNotificationRepository.class);
        Ubs matchingClinic = new Ubs("Central", "Rua A", "São Paulo", "SP", null, null);
        org.springframework.test.util.ReflectionTestUtils.setField(matchingClinic, "id", 1L);
        Ubs otherClinic = new Ubs("North", "Rua B", "São Paulo", "SP", null, null);
        org.springframework.test.util.ReflectionTestUtils.setField(otherClinic, "id", 2L);
        AppUser citizen = new AppUser("Ana", "ana@example.com", "encoded", Role.CITIZEN);
        Medication medication = new Medication("Paracetamol", "paracetamol", null);
        List<StockAlert> matches = List.of(
                new StockAlert(citizen, medication, matchingClinic),
                new StockAlert(citizen, medication, null),
                new StockAlert(citizen, medication, otherClinic));
        when(alerts.findByMedicationIdAndActiveTrue(4L)).thenReturn(matches);

        new StockReplenishedListener(alerts, notifications).onReplenished(
                new StockReplenishedEvent(7L, 4L, "Paracetamol", 1L, "Central"));

        verify(notifications, times(2)).save(any());
    }
}
