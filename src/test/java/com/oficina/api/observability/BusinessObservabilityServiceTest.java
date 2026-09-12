package com.oficina.api.observability;

import com.oficina.api.config.CorrelationIdFilter;
import com.oficina.api.model.OrdemServico;
import com.oficina.api.model.enums.StatusOrdemServico;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class BusinessObservabilityServiceTest {

    private final CapturingNewRelicClient newRelicClient = new CapturingNewRelicClient();
    private final BusinessObservabilityService observabilityService = new BusinessObservabilityService(newRelicClient);

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void deveRegistrarEventoEMetricaDeCriacaoDeOrdemServico() {
        MDC.put(CorrelationIdFilter.CORRELATION_ID_MDC_KEY, "correlation-123");
        OrdemServico ordemServico = new OrdemServico();
        ordemServico.setId(10L);
        ordemServico.setStatus(StatusOrdemServico.RECEBIDA);

        observabilityService.recordOrdemServicoCreated(ordemServico);

        assertThat(newRelicClient.eventType).isEqualTo(BusinessObservabilityService.EVENT_ORDER_CREATED);
        assertThat(newRelicClient.attributes)
                .containsEntry("correlationId", "correlation-123")
                .containsEntry("ordemServicoId", 10L)
                .containsEntry("status", "RECEBIDA");
        assertThat(newRelicClient.metricName).isEqualTo(BusinessObservabilityService.METRIC_ORDER_CREATED);
        assertThat(newRelicClient.metricValue).isEqualTo(1.0F);
    }

    @Test
    void deveRegistrarDuracaoDeEtapa() {
        observabilityService.recordStageDuration(10L, StatusOrdemServico.EM_EXECUCAO, Duration.ofMinutes(15));

        assertThat(newRelicClient.eventType).isEqualTo(BusinessObservabilityService.EVENT_STAGE_DURATION);
        assertThat(newRelicClient.attributes)
                .containsEntry("ordemServicoId", 10L)
                .containsEntry("stage", "EM_EXECUCAO")
                .containsEntry("durationMillis", 900000L)
                .containsEntry("durationSeconds", 900.0D);
        assertThat(newRelicClient.responseTimeMetricName)
                .isEqualTo(BusinessObservabilityService.METRIC_STAGE_DURATION_PREFIX + "EM_EXECUCAO");
        assertThat(newRelicClient.responseTimeMetricMillis).isEqualTo(900000L);
    }

    @Test
    void deveRegistrarErroDeIntegracaoExternaSemMensagemSensivel() {
        observabilityService.recordExternalIntegrationError(
                "email",
                "approval-email",
                new IllegalStateException("mensagem nao deve ser enviada")
        );

        assertThat(newRelicClient.eventType).isEqualTo(BusinessObservabilityService.EVENT_EXTERNAL_INTEGRATION_ERROR);
        assertThat(newRelicClient.attributes)
                .containsEntry("integrationType", "email")
                .containsEntry("operation", "approval-email")
                .containsEntry("errorType", "IllegalStateException")
                .doesNotContainKey("message");
    }

    private static class CapturingNewRelicClient implements NewRelicClient {

        private String eventType;
        private Map<String, Object> attributes;
        private String metricName;
        private float metricValue;
        private String responseTimeMetricName;
        private long responseTimeMetricMillis;

        @Override
        public void recordCustomEvent(String eventType, Map<String, ?> attributes) {
            this.eventType = eventType;
            this.attributes = new LinkedHashMap<>(attributes);
        }

        @Override
        public void recordMetric(String name, float value) {
            this.metricName = name;
            this.metricValue = value;
        }

        @Override
        public void recordResponseTimeMetric(String name, long millis) {
            this.responseTimeMetricName = name;
            this.responseTimeMetricMillis = millis;
        }
    }
}
