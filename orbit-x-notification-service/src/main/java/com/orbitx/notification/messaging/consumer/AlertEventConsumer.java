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

/**
 * RabbitMQ consumers for Orbit X alert events.
 *
 * Two consumers with Manual ACK:
 *  1. alertsConsumer  — general HIGH/CRITICAL alerts → email dispatch
 *  2. thermalConsumer — thermal threshold breaches → immediate ops notification
 *
 * Why manual ACK?
 *  - Ensures the message is only acknowledged after successful processing.
 *  - If email fails, the message is NACK'd and re-queued (up to 3 retries per RabbitMQConfig).
 *  - Prevents silent loss of critical alerts.
 */
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
        log.info("Alert event received — id={} severity={}", event.eventId(), event.severity());
        try {
            notificationService.sendEmailAlert(event);
            channel.basicAck(deliveryTag, false);
            log.debug("Alert event ACK'd — id={}", event.eventId());
        } catch (Exception ex) {
            log.error("Processing failed for event id={} — re-queuing: {}", event.eventId(), ex.getMessage());
            // requeue=false: sends to DLX after max retry exhaustion
            channel.basicNack(deliveryTag, false, false);
        }
    }

    @RabbitListener(
            queues    = "${orbitx.messaging.thermal-queue}",
            ackMode   = "MANUAL",
            concurrency = "2"  // parallel consumers for the high-priority thermal lane
    )
    public void consumeThermalCritical(AlertEvent event,
                                       Channel channel,
                                       @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        log.warn("THERMAL CRITICAL received — id={} temp={}°C prob={}%%",
                event.eventId(),
                event.currentTemperatureCelsius(),
                Math.round(event.overheatProbability() * 100));
        try {
            notificationService.logThermalCritical(event);
            notificationService.sendEmailAlert(event);
            channel.basicAck(deliveryTag, false);
        } catch (Exception ex) {
            log.error("Thermal consumer failed — id={}: {}", event.eventId(), ex.getMessage());
            channel.basicNack(deliveryTag, false, false);
        }
    }
}
