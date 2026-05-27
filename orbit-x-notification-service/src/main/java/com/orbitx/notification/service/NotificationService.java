package com.orbitx.notification.service;

import com.orbitx.notification.messaging.dto.AlertEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Handles dispatch of alert notifications.
 *
 * Supported channels:
 *  - Email (via Spring Mail / SMTP)
 *  - Webhook (Slack / PagerDuty / Teams) — HTTP POST via RestClient
 *  - In-app push (future: FCM via Firebase Admin SDK)
 *
 * The service is intentionally thin — it delegates formatting to templates
 * and dispatch to the mail sender. Business logic stays in the consumer.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final JavaMailSender mailSender;

    @Value("${orbitx.notifications.ops-email}")
    private String opsEmail;

    public void sendEmailAlert(AlertEvent event) {
        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo(opsEmail);
            mail.setFrom("alerts@orbitx.io");
            mail.setSubject("[Orbit X] %s Alert — %s".formatted(event.severity(), event.title()));
            mail.setText(buildEmailBody(event));
            mailSender.send(mail);
            log.info("Email alert sent — eventId={} severity={}", event.eventId(), event.severity());
        } catch (Exception ex) {
            log.error("Failed to send email for eventId={}: {}", event.eventId(), ex.getMessage());
        }
    }

    public void logThermalCritical(AlertEvent event) {
        // In production: POST to Slack webhook, PagerDuty, OpsGenie, etc.
        log.error("""
                ┌─────────────────────────────────────────────────┐
                │           ORBIT X — THERMAL CRITICAL            │
                ├─────────────────────────────────────────────────┤
                │  Event ID   : {}
                │  Temperature: {} °C
                │  Risk       : {}%%
                │  Message    : {}
                │  Occurred   : {}
                └─────────────────────────────────────────────────┘
                """,
                event.eventId(),
                event.currentTemperatureCelsius(),
                Math.round(event.overheatProbability() * 100),
                event.message(),
                event.occurredAt()
        );
    }

    private String buildEmailBody(AlertEvent event) {
        String ts = event.occurredAt() != null
                ? DateTimeFormatter.ISO_LOCAL_DATE_TIME
                        .withZone(ZoneId.of("UTC"))
                        .format(event.occurredAt())
                : "N/A";
        return """
                Orbit X — Intelligent Datacenter Alert
                ═══════════════════════════════════════

                Severity    : %s
                Title       : %s
                Source      : %s
                Datacenter  : %s
                Temperature : %.1f °C
                Risk Level  : %.1f%%
                Occurred At : %s (UTC)

                Message:
                %s

                ───────────────────────────────────────
                Orbit X AI Monitoring Engine
                This is an automated alert. Do not reply.
                """.formatted(
                event.severity(),
                event.title(),
                event.sourceComponent(),
                event.datacenterId() != null ? "DC-" + event.datacenterId() : "Global",
                event.currentTemperatureCelsius(),
                event.overheatProbability() * 100,
                ts,
                event.message()
        );
    }
}
