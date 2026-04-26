package com.oficina.api.service;

import com.oficina.api.dto.request.servico.ServicoCreateRequest;
import com.oficina.api.dto.request.servico.ServicoUpdateRequest;
import com.oficina.api.exception.ResourceNotFoundException;
import com.oficina.api.exception.ValidationException;
import com.oficina.api.model.Servico;
import com.oficina.api.repository.ServicoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServicoServiceTest {

    @Mock
    private ServicoRepository servicoRepository;

    @InjectMocks
    private ServicoService servicoService;

    @BeforeEach
    void setUp() {
        lenient().when(servicoRepository.save(any(Servico.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void deveCriarServicoComSucesso() {
        ServicoCreateRequest request = new ServicoCreateRequest("Troca de oleo", new BigDecimal("120.00"), 60);

        Servico resultado = servicoService.criar(request);

        assertThat(resultado.getDescricao()).isEqualTo("Troca de oleo");
        assertThat(resultado.getValor()).isEqualByComparingTo("120.00");
        assertThat(resultado.getTempoEstimadoMinutos()).isEqualTo(60);
        assertThat(resultado.getAtivo()).isTrue();
        verify(servicoRepository).save(any(Servico.class));
    }

    @Test
    void deveLancarValidationExceptionAoCriarComDescricaoVazia() {
        ServicoCreateRequest request = new ServicoCreateRequest("   ", new BigDecimal("120.00"), 60);

        assertThrows(ValidationException.class, () -> servicoService.criar(request));

        verify(servicoRepository, never()).save(any(Servico.class));
    }

    @Test
    void deveLancarValidationExceptionAoCriarComValorNegativo() {
        ServicoCreateRequest request = new ServicoCreateRequest("Troca de oleo", new BigDecimal("-1.00"), 60);

        assertThrows(ValidationException.class, () -> servicoService.criar(request));

        verify(servicoRepository, never()).save(any(Servico.class));
    }

    @Test
    void deveLancarValidationExceptionAoCriarComTempoEstimadoInvalido() {
        ServicoCreateRequest request = new ServicoCreateRequest("Troca de oleo", new BigDecimal("120.00"), 0);

        assertThrows(ValidationException.class, () -> servicoService.criar(request));

        verify(servicoRepository, never()).save(any(Servico.class));
    }

    @Test
    void deveBuscarPorIdComSucesso() {
        Servico servico = criarServico(1L);
        when(servicoRepository.findById(1L)).thenReturn(Optional.of(servico));

        Servico resultado = servicoService.buscarPorId(1L);

        assertThat(resultado).isEqualTo(servico);
        verify(servicoRepository).findById(1L);
    }

    @Test
    void deveLancarResourceNotFoundExceptionAoBuscarIdInexistente() {
        when(servicoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> servicoService.buscarPorId(1L));

        verify(servicoRepository).findById(1L);
    }

    @Test
    void deveListarServicos() {
        when(servicoRepository.findAll()).thenReturn(List.of(criarServico(1L), criarServico(2L)));

        List<Servico> resultado = servicoService.listar();

        assertThat(resultado).hasSize(2);
        verify(servicoRepository).findAll();
    }

    @Test
    void deveListarServicosAtivos() {
        when(servicoRepository.findByAtivoTrue()).thenReturn(List.of(criarServico(1L)));

        List<Servico> resultado = servicoService.listarAtivos();

        assertThat(resultado).hasSize(1);
        verify(servicoRepository).findByAtivoTrue();
    }

    @Test
    void deveAtualizarComSucesso() {
        Servico servico = criarServico(1L);
        ServicoUpdateRequest request = new ServicoUpdateRequest("Alinhamento", new BigDecimal("200.00"), 90, false);
        when(servicoRepository.findById(1L)).thenReturn(Optional.of(servico));

        Servico resultado = servicoService.atualizar(1L, request);

        assertThat(resultado.getDescricao()).isEqualTo("Alinhamento");
        assertThat(resultado.getValor()).isEqualByComparingTo("200.00");
        assertThat(resultado.getTempoEstimadoMinutos()).isEqualTo(90);
        assertThat(resultado.getAtivo()).isFalse();
        verify(servicoRepository).findById(1L);
        verify(servicoRepository).save(any(Servico.class));
    }

    @Test
    void deveLancarResourceNotFoundExceptionAoAtualizarServicoInexistente() {
        ServicoUpdateRequest request = new ServicoUpdateRequest("Alinhamento", new BigDecimal("200.00"), 90, false);
        when(servicoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> servicoService.atualizar(1L, request));

        verify(servicoRepository).findById(1L);
        verify(servicoRepository, never()).save(any(Servico.class));
    }

    @Test
    void deveLancarValidationExceptionAoAtualizarComValorNegativo() {
        Servico servico = criarServico(1L);
        ServicoUpdateRequest request = new ServicoUpdateRequest("Alinhamento", new BigDecimal("-1.00"), 90, false);
        when(servicoRepository.findById(1L)).thenReturn(Optional.of(servico));

        assertThrows(ValidationException.class, () -> servicoService.atualizar(1L, request));

        verify(servicoRepository).findById(1L);
        verify(servicoRepository, never()).save(any(Servico.class));
    }

    @Test
    void deveExcluirComSucesso() {
        Servico servico = criarServico(1L);
        when(servicoRepository.findById(1L)).thenReturn(Optional.of(servico));

        servicoService.excluir(1L);

        verify(servicoRepository).findById(1L);
        verify(servicoRepository).delete(servico);
    }

    @Test
    void deveLancarResourceNotFoundExceptionAoExcluirServicoInexistente() {
        when(servicoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> servicoService.excluir(1L));

        verify(servicoRepository).findById(1L);
        verify(servicoRepository, never()).delete(any(Servico.class));
    }

    private Servico criarServico(Long id) {
        Servico servico = new Servico();
        servico.setId(id);
        servico.setDescricao("Troca de oleo");
        servico.setValor(new BigDecimal("120.00"));
        servico.setTempoEstimadoMinutos(60);
        servico.setAtivo(true);
        return servico;
    }
}
