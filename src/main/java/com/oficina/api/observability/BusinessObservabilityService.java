package com.oficina.api.observability;

import com.oficina.api.config.CorrelationIdFilter;
import com.oficina.api.model.OrdemServico;
import com.oficina.api.model.enums.StatusOrdemServico;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class BusinessObservabilityService {

    public static final String EVENT_ORDER_CREATED = "OrdemServicoCreated";
    public static final String EVENT_STATUS_CHANGED = "OrdemServicoStatusChanged";
    public static final String EVENT_STAGE_DURATION = "OrdemServicoStageDuration";
    public static final String EVENT_EXTERNAL_INTEGRATION_ERROR = "ExternalIntegrationError";

    public static final String METRIC_ORDER_CREATED = "Custom/Business/OrdemServico/Created";
    public static final String METRIC_STAGE_DURATION_PREFIX = "Custom/Business/OrdemServico/StageDuration/";

    private static final Logger logger = LoggerFactory.getLogger(BusinessObservabilityService.class);

    private final NewRelicClient newRelicClient;

    public BusinessObservabilityService(NewRelicClient newRelicClient) {
        this.newRelicClient = newRelicClient;
    }

    public void recordOrdemServicoCreated(OrdemServico ordemServico) {
        Map<String, Object> attributes = baseAttributes();
        attributes.put("ordemServicoId", ordemServico.getId());
        attributes.put("status", safeStatus(ordemServico.getStatus()));

        recordEvent(EVENT_ORDER_CREATED, attributes);
        recordMetric(METRIC_ORDER_CREATED, 1.0F);
    }

    public void recordStatusChanged(Long ordemServicoId, StatusOrdemServico previousStatus, StatusOrdemServico newStatus) {
        Map<String, Object> attributes = baseAttributes();
        attributes.put("ordemServicoId", ordemServicoId);
        attributes.put("previousStatus", safeStatus(previousStatus));
        attributes.put("newStatus", safeStatus(newStatus));

        recordEvent(EVENT_STATUS_CHANGED, attributes);
    }

    public void recordStageDuration(Long ordemServicoId, StatusOrdemServico stage, Duration duration) {
        if (stage == null || duration == null || duration.isNegative()) {
            return;
        }

        long durationMillis = duration.toMillis();
        Map<String, Object> attributes = baseAttributes();
        attributes.put("ordemServicoId", ordemServicoId);
        attributes.put("stage", safeStatus(stage));
        attributes.put("durationMillis", durationMillis);
        attributes.put("durationSeconds", duration.toMillis() / 1000.0D);

        recordEvent(EVENT_STAGE_DURATION, attributes);
        recordResponseTimeMetric(METRIC_STAGE_DURATION_PREFIX + stage.name(), durationMillis);
    }

    public void recordExternalIntegrationError(String integrationType, String operation, Exception exception) {
        Map<String, Object> attributes = baseAttributes();
        attributes.put("integrationType", integrationType);
        attributes.put("operation", operation);
        attributes.put("errorType", exception.getClass().getSimpleName());

        Throwable cause = exception.getCause();
        if (cause != null) {
            attributes.put("causeType", cause.getClass().getSimpleName());
        }

        recordEvent(EVENT_EXTERNAL_INTEGRATION_ERROR, attributes);
    }

    private Map<String, Object> baseAttributes() {
        Map<String, Object> attributes = new LinkedHashMap<>();
        String correlationId = MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY);
        if (correlationId != null && !correlationId.isBlank()) {
            attributes.put("correlationId", correlationId);
        }
        return attributes;
    }

    private void recordEvent(String eventType, Map<String, ?> attributes) {
        try {
            newRelicClient.recordCustomEvent(eventType, attributes);
        } catch (RuntimeException ex) {
            logger.debug("Falha ao registrar evento de observabilidade no New Relic.", ex);
        }
    }

    private void recordMetric(String name, float value) {
        try {
            newRelicClient.recordMetric(name, value);
        } catch (RuntimeException ex) {
            logger.debug("Falha ao registrar metrica de observabilidade no New Relic.", ex);
        }
    }

    private void recordResponseTimeMetric(String name, long millis) {
        try {
            newRelicClient.recordResponseTimeMetric(name, millis);
        } catch (RuntimeException ex) {
            logger.debug("Falha ao registrar metrica de tempo no New Relic.", ex);
        }
    }

    private String safeStatus(StatusOrdemServico status) {
        return status == null ? "UNKNOWN" : status.name();
    }
}
