package com.oficina.api.config;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CorrelationIdFilterTest {

    private final CorrelationIdFilter filter = new CorrelationIdFilter();

    @AfterEach
    void limparMdc() {
        MDC.clear();
    }

    @Test
    void deveReutilizarCorrelationIdDoHeader() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader(CorrelationIdFilter.CORRELATION_ID_HEADER, "correlation-id-existente");

        filter.doFilter(request, response, assertMdcCorrelationId("correlation-id-existente"));

        assertThat(response.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER)).isEqualTo("correlation-id-existente");
    }

    @Test
    void deveGerarUuidQuandoHeaderNaoForInformado() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        String[] generatedCorrelationId = new String[1];

        filter.doFilter(request, response, (servletRequest, servletResponse) -> {
            String correlationId = MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY);

            assertThat(correlationId).isNotBlank();
            assertThat(UUID.fromString(correlationId)).isNotNull();
            generatedCorrelationId[0] = correlationId;
        });

        assertThat(response.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER)).isEqualTo(generatedCorrelationId[0]);
    }

    @Test
    void deveLimparMdcAoFinalDaRequisicao() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader(CorrelationIdFilter.CORRELATION_ID_HEADER, "correlation-id-limpeza");

        filter.doFilter(request, response, assertMdcCorrelationId("correlation-id-limpeza"));

        assertThat(MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY)).isNull();
    }

    @Test
    void deveLimparMdcMesmoQuandoCadeiaLancarExcecao() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader(CorrelationIdFilter.CORRELATION_ID_HEADER, "correlation-id-erro");

        assertThatThrownBy(() -> filter.doFilter(request, response, (servletRequest, servletResponse) -> {
            throw new ServletException("erro simulado");
        })).isInstanceOf(ServletException.class);

        assertThat(MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY)).isNull();
    }

    @Test
    void deveRegistrarLogAoFinalDaRequisicaoComCorrelationIdNoMdc() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/actuator/health/readiness");
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader(CorrelationIdFilter.CORRELATION_ID_HEADER, "correlation-id-log");
        response.setStatus(200);
        Logger logger = (Logger) LoggerFactory.getLogger(CorrelationIdFilter.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);

        try {
            filter.doFilter(request, response, assertMdcCorrelationId("correlation-id-log"));
        } finally {
            logger.detachAppender(appender);
        }

        assertThat(appender.list).hasSize(1);
        ILoggingEvent loggingEvent = appender.list.getFirst();
        assertThat(loggingEvent.getFormattedMessage())
                .matches("HTTP request completed: method=GET path=/actuator/health/readiness status=200 durationMs=\\d+");
        assertThat(loggingEvent.getMDCPropertyMap())
                .containsEntry(CorrelationIdFilter.CORRELATION_ID_MDC_KEY, "correlation-id-log");
    }

    private FilterChain assertMdcCorrelationId(String expectedCorrelationId) {
        return (servletRequest, servletResponse) ->
                assertThat(MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY)).isEqualTo(expectedCorrelationId);
    }
}
