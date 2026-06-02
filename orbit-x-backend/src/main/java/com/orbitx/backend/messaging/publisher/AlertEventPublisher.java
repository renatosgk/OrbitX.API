package com.orbitx.backend.messaging.publisher;

import com.orbitx.backend.messaging.dto.AlertEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.rabbitmq.enabled", havingValue = "true", matchIfMissing = true)
public class AlertEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${orbitx.messaging.exchange}")
    private String exchange;

    @Value("${orbitx.messaging.alerts-routing-key}")
    private String alertsRoutingKey;

    @Value("${orbitx.messaging.thermal-routing-key}")
    private String thermalRoutingKey;

    public void publishAlert(AlertEvent event) {
        try {
            rabbitTemplate.convertAndSend(exchange, alertsRoutingKey, event);
            log.info("Evento de alerta publicado — id={} severidade={} componente={}",
                    event.eventId(), event.severity(), event.sourceComponent());
        } catch (AmqpException ex) {
            log.error("Falha ao publicar evento de alerta id={}: {}", event.eventId(), ex.getMessage());
        }
    }

    public void publishThermalCritical(AlertEvent event) {
        try {
            rabbitTemplate.convertAndSend(exchange, thermalRoutingKey, event);
            log.warn("Evento TÉRMICO CRÍTICO publicado — temp={}°C prob={}",
                    event.currentTemperatureCelsius(), event.overheatProbability());
        } catch (AmqpException ex) {
            log.error("Falha ao publicar evento térmico: {}", ex.getMessage());
        }
    }
}
