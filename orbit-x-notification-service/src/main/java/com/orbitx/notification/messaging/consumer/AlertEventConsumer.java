package com.orbitx.notification.messaging.consumer;

import com.orbitx.notification.messaging.dto.AlertEvent;
import com.orbitx.notification.service.NotificationService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlertEventConsumer {

    private final NotificationService notificationService;

    @RabbitListener(
            queues    = "${orbitx.messaging.alerts-queue}",
            ackMode   = "MANUAL"
    )
    public void consumeAlert(AlertEvent event,
                             Channel channel,
                             @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        log.info("Evento de alerta recebido — id={} severidade={}", event.eventId(), event.severity());
        try {
            notificationService.sendEmailAlert(event);
            channel.basicAck(deliveryTag, false);
            log.debug("Evento de alerta confirmado (ACK) — id={}", event.eventId());
        } catch (Exception ex) {
            log.error("Falha ao processar evento id={} — reenfileirando: {}", event.eventId(), ex.getMessage());
            
            channel.basicNack(deliveryTag, false, false);
        }
    }

    @RabbitListener(
            queues    = "${orbitx.messaging.thermal-queue}",
            ackMode   = "MANUAL",
            concurrency = "2"  
    )
    public void consumeThermalCritical(AlertEvent event,
                                       Channel channel,
                                       @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        log.warn("TÉRMICO CRÍTICO recebido — id={} temp={}°C prob={}%%",
                event.eventId(),
                event.currentTemperatureCelsius(),
                Math.round(event.overheatProbability() * 100));
        try {
            notificationService.logThermalCritical(event);
            notificationService.sendEmailAlert(event);
            channel.basicAck(deliveryTag, false);
        } catch (Exception ex) {
            log.error("Falha no consumidor térmico — id={}: {}", event.eventId(), ex.getMessage());
            channel.basicNack(deliveryTag, false, false);
        }
    }
}
