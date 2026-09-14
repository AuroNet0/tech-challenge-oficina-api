package com.oficina.api.service;

import com.oficina.api.dto.request.ordemservico.AdicionarServicoOrdemRequest;
import com.oficina.api.model.Cliente;
import com.oficina.api.model.OrdemServico;
import com.oficina.api.model.Servico;
import com.oficina.api.model.enums.StatusOrdemServico;
import com.oficina.api.observability.BusinessObservabilityService;
import com.oficina.api.repository.ClienteRepository;
import com.oficina.api.repository.OrdemServicoRepository;
import com.oficina.api.repository.PecaRepository;
import com.oficina.api.repository.ServicoRepository;
import com.oficina.api.repository.VeiculoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
class EmailServiceTest {

    @Test
    void deveIgnorarFalhaDeEmailAoEnviarAtualizacaoStatus() {
        JavaMailSender javaMailSender = mock(JavaMailSender.class);
        BusinessObservabilityService observabilityService = mock(BusinessObservabilityService.class);
        MailSendException exception = new MailSendException("smtp indisponivel");

        org.mockito.Mockito.doThrow(exception).when(javaMailSender).send(any(SimpleMailMessage.class));

        EmailService emailService = new EmailService(
                javaMailSenderProvider(javaMailSender),
                "http://localhost:8080",
                "noreply@oficina.com",
                observabilityService
        );

        assertDoesNotThrow(() -> emailService.enviarAtualizacaoStatus(criarOrdemServico(StatusOrdemServico.EM_DIAGNOSTICO)));

        verify(javaMailSender).send(any(SimpleMailMessage.class));
        verify(observabilityService).recordExternalIntegrationError(eq("email"), eq("status-update-email"), eq(exception));
    }

    @Test
    void deveManterAtualizacaoDaOsQuandoEmailFalharAoAdicionarServico() {
        OrdemServicoRepository ordemServicoRepository = mock(OrdemServicoRepository.class);
        ClienteRepository clienteRepository = mock(ClienteRepository.class);
        VeiculoRepository veiculoRepository = mock(VeiculoRepository.class);
        ServicoRepository servicoRepository = mock(ServicoRepository.class);
        PecaRepository pecaRepository = mock(PecaRepository.class);
        TokenAprovacaoService tokenAprovacaoService = mock(TokenAprovacaoService.class);
        BusinessObservabilityService observabilityService = mock(BusinessObservabilityService.class);
        JavaMailSender javaMailSender = mock(JavaMailSender.class);
        MailSendException exception = new MailSendException("smtp indisponivel");
        OrdemServico ordemServico = criarOrdemServico(StatusOrdemServico.RECEBIDA);
        Servico servico = criarServico();

        org.mockito.Mockito.doThrow(exception).when(javaMailSender).send(any(SimpleMailMessage.class));
        when(ordemServicoRepository.findById(1L)).thenReturn(Optional.of(ordemServico));
        when(servicoRepository.findById(1L)).thenReturn(Optional.of(servico));
        when(ordemServicoRepository.save(any(OrdemServico.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EmailService emailService = new EmailService(
                javaMailSenderProvider(javaMailSender),
                "http://localhost:8080",
                "noreply@oficina.com",
                observabilityService
        );
        OrdemServicoService ordemServicoService = new OrdemServicoService(
                ordemServicoRepository,
                clienteRepository,
                veiculoRepository,
                servicoRepository,
                pecaRepository,
                tokenAprovacaoService,
                emailService,
                observabilityService
        );

        OrdemServico resultado = assertDoesNotThrow(() -> ordemServicoService.adicionarServico(
                1L,
                new AdicionarServicoOrdemRequest(1L, "blaublau")
        ));

        assertThat(resultado.getStatus()).isEqualTo(StatusOrdemServico.EM_DIAGNOSTICO);
        assertThat(resultado.getItensServico()).hasSize(1);
        assertThat(resultado.getValorTotal()).isEqualByComparingTo("120.00");
        verify(ordemServicoRepository).save(ordemServico);
        verify(observabilityService).recordExternalIntegrationError(eq("email"), eq("status-update-email"), eq(exception));
    }

    @Test
    void deveEnviarAtualizacaoStatusNormalmenteQuandoEmailFuncionar() {
        JavaMailSender javaMailSender = mock(JavaMailSender.class);
        BusinessObservabilityService observabilityService = mock(BusinessObservabilityService.class);

        EmailService emailService = new EmailService(
                javaMailSenderProvider(javaMailSender),
                "http://localhost:8080",
                "noreply@oficina.com",
                observabilityService
        );

        emailService.enviarAtualizacaoStatus(criarOrdemServico(StatusOrdemServico.EM_DIAGNOSTICO));

        verify(javaMailSender).send(any(SimpleMailMessage.class));
    }

    private ObjectProvider<JavaMailSender> javaMailSenderProvider(JavaMailSender javaMailSender) {
        return new ObjectProvider<>() {
            @Override
            public JavaMailSender getObject(Object... args) {
                return javaMailSender;
            }

            @Override
            public JavaMailSender getIfAvailable() {
                return javaMailSender;
            }

            @Override
            public JavaMailSender getObject() {
                return javaMailSender;
            }
        };
    }

    private OrdemServico criarOrdemServico(StatusOrdemServico status) {
        Cliente cliente = new Cliente();
        cliente.setNome("Cliente Teste");
        cliente.setEmail("cliente@oficina.com");

        OrdemServico ordemServico = new OrdemServico();
        ordemServico.setId(1L);
        ordemServico.setCliente(cliente);
        ordemServico.setStatus(status);
        ordemServico.setValorTotal(BigDecimal.ZERO);
        ordemServico.setItensServico(new ArrayList<>());
        ordemServico.setItensPeca(new ArrayList<>());
        return ordemServico;
    }

    private Servico criarServico() {
        Servico servico = new Servico();
        servico.setId(1L);
        servico.setDescricao("Troca de oleo");
        servico.setValor(new BigDecimal("120.00"));
        servico.setTempoEstimadoMinutos(60);
        servico.setAtivo(true);
        return servico;
    }
}
