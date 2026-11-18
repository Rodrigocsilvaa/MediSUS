package br.com.medisus.api.messaging;

import br.com.medisus.api.domain.StockAlert;
import br.com.medisus.api.domain.AppNotification;
import br.com.medisus.api.repository.AppNotificationRepository;
import br.com.medisus.api.repository.StockAlertRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class StockReplenishedListener {
    private final StockAlertRepository alerts;
    private final AppNotificationRepository notifications;

    public StockReplenishedListener(StockAlertRepository alerts, AppNotificationRepository notifications) {
        this.alerts = alerts; this.notifications = notifications;
    }

    @RabbitListener(queues = RabbitConfiguration.STOCK_REPLENISHED_QUEUE)
    @Transactional
    public void onReplenished(StockReplenishedEvent event) {
        for (StockAlert alert : alerts.findByMedicationIdAndActiveTrue(event.medicationId())) {
            if (alert.getUbs() == null || alert.getUbs().getId().equals(event.ubsId())) {
                String message = event.medicationName() + " is available at " + event.ubsName() + ".";
                notifications.save(new AppNotification(alert.getUser(), message));
            }
        }
    }
}
