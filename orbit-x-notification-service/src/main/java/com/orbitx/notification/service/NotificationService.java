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
            mail.setSubject("[Orbit X] Alerta %s — %s".formatted(event.severity(), event.title()));
            mail.setText(buildEmailBody(event));
            mailSender.send(mail);
            log.info("Alerta por e-mail enviado — eventId={} severidade={}", event.eventId(), event.severity());
        } catch (Exception ex) {
            log.error("Falha ao enviar e-mail para eventId={}: {}", event.eventId(), ex.getMessage());
        }
    }

    public void logThermalCritical(AlertEvent event) {
        
        log.error("""
                ┌─────────────────────────────────────────────────┐
                │           ORBIT X — CRÍTICO TÉRMICO             │
                ├─────────────────────────────────────────────────┤
                │  ID do Evento  : {}
                │  Temperatura   : {} °C
                │  Risco         : {}%%
                │  Mensagem      : {}
                │  Ocorrido em   : {}
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
                Orbit X — Alerta Inteligente de Datacenter
                ═══════════════════════════════════════════

                Severidade   : %s
                Título       : %s
                Origem       : %s
                Datacenter   : %s
                Temperatura  : %.1f °C
                Nível de Risco: %.1f%%
                Ocorrido em  : %s (UTC)

                Mensagem:
                %s

                ───────────────────────────────────────────
                Motor de Monitoramento IA — Orbit X
                Este é um alerta automático. Não responda.
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
