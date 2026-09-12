package com.oficina.api.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
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

    private FilterChain assertMdcCorrelationId(String expectedCorrelationId) {
        return (servletRequest, servletResponse) ->
                assertThat(MDC.get(CorrelationIdFilter.CORRELATION_ID_MDC_KEY)).isEqualTo(expectedCorrelationId);
    }
}
