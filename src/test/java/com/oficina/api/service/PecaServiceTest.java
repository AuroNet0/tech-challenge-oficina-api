package com.oficina.api.service;

import com.oficina.api.dto.request.peca.AtualizarEstoqueRequest;
import com.oficina.api.dto.request.peca.PecaCreateRequest;
import com.oficina.api.dto.request.peca.PecaUpdateRequest;
import com.oficina.api.exception.ResourceNotFoundException;
import com.oficina.api.exception.ValidationException;
import com.oficina.api.model.Peca;
import com.oficina.api.repository.PecaRepository;
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
class PecaServiceTest {

    @Mock
    private PecaRepository pecaRepository;

    @InjectMocks
    private PecaService pecaService;

    @BeforeEach
    void setUp() {
        lenient().when(pecaRepository.save(any(Peca.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void deveCriarPecaComSucesso() {
        PecaCreateRequest request = new PecaCreateRequest("Filtro", "Filtro de oleo", new BigDecimal("25.00"), 10);

        Peca resultado = pecaService.criar(request);

        assertThat(resultado.getNome()).isEqualTo("Filtro");
        assertThat(resultado.getDescricao()).isEqualTo("Filtro de oleo");
        assertThat(resultado.getValorUnitario()).isEqualByComparingTo("25.00");
        assertThat(resultado.getQuantidadeEstoque()).isEqualTo(10);
        assertThat(resultado.getAtivo()).isTrue();
        verify(pecaRepository).save(any(Peca.class));
    }

    @Test
    void deveLancarValidationExceptionAoCriarComNomeVazio() {
        PecaCreateRequest request = new PecaCreateRequest("  ", "Filtro de oleo", new BigDecimal("25.00"), 10);

        assertThrows(ValidationException.class, () -> pecaService.criar(request));
        verify(pecaRepository, never()).save(any(Peca.class));
    }

    @Test
    void deveLancarValidationExceptionAoCriarComValorUnitarioNegativo() {
        PecaCreateRequest request = new PecaCreateRequest("Filtro", "Filtro de oleo", new BigDecimal("-1.00"), 10);

        assertThrows(ValidationException.class, () -> pecaService.criar(request));
        verify(pecaRepository, never()).save(any(Peca.class));
    }

    @Test
    void deveLancarValidationExceptionAoCriarComQuantidadeEstoqueNegativa() {
        PecaCreateRequest request = new PecaCreateRequest("Filtro", "Filtro de oleo", new BigDecimal("25.00"), -1);

        assertThrows(ValidationException.class, () -> pecaService.criar(request));
        verify(pecaRepository, never()).save(any(Peca.class));
    }

    @Test
    void deveBuscarPorIdComSucesso() {
        Peca peca = criarPeca(1L);
        when(pecaRepository.findById(1L)).thenReturn(Optional.of(peca));

        Peca resultado = pecaService.buscarPorId(1L);

        assertThat(resultado).isEqualTo(peca);
        verify(pecaRepository).findById(1L);
    }

    @Test
    void deveLancarResourceNotFoundExceptionAoBuscarIdInexistente() {
        when(pecaRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> pecaService.buscarPorId(1L));
        verify(pecaRepository).findById(1L);
    }

    @Test
    void deveListarPecas() {
        when(pecaRepository.findAll()).thenReturn(List.of(criarPeca(1L), criarPeca(2L)));

        List<Peca> resultado = pecaService.listar();

        assertThat(resultado).hasSize(2);
        verify(pecaRepository).findAll();
    }

    @Test
    void deveListarPecasAtivas() {
        when(pecaRepository.findByAtivoTrue()).thenReturn(List.of(criarPeca(1L)));

        List<Peca> resultado = pecaService.listarAtivas();

        assertThat(resultado).hasSize(1);
        verify(pecaRepository).findByAtivoTrue();
    }

    @Test
    void deveListarPecasComEstoqueBaixo() {
        when(pecaRepository.findByQuantidadeEstoqueLessThanEqual(5)).thenReturn(List.of(criarPeca(1L)));

        List<Peca> resultado = pecaService.listarComEstoqueBaixo(5);

        assertThat(resultado).hasSize(1);
        verify(pecaRepository).findByQuantidadeEstoqueLessThanEqual(5);
    }

    @Test
    void deveAtualizarComSucesso() {
        Peca peca = criarPeca(1L);
        PecaUpdateRequest request = new PecaUpdateRequest("Pastilha", "Pastilha freio", new BigDecimal("80.00"), false);
        when(pecaRepository.findById(1L)).thenReturn(Optional.of(peca));

        Peca resultado = pecaService.atualizar(1L, request);

        assertThat(resultado.getNome()).isEqualTo("Pastilha");
        assertThat(resultado.getDescricao()).isEqualTo("Pastilha freio");
        assertThat(resultado.getValorUnitario()).isEqualByComparingTo("80.00");
        assertThat(resultado.getAtivo()).isFalse();
        assertThat(resultado.getQuantidadeEstoque()).isEqualTo(10);
        verify(pecaRepository).findById(1L);
        verify(pecaRepository).save(any(Peca.class));
    }

    @Test
    void deveLancarResourceNotFoundExceptionAoAtualizarPecaInexistente() {
        PecaUpdateRequest request = new PecaUpdateRequest("Pastilha", "Pastilha freio", new BigDecimal("80.00"), false);
        when(pecaRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> pecaService.atualizar(1L, request));

        verify(pecaRepository).findById(1L);
        verify(pecaRepository, never()).save(any(Peca.class));
    }

    @Test
    void deveLancarValidationExceptionAoAtualizarComValorUnitarioNegativo() {
        Peca peca = criarPeca(1L);
        PecaUpdateRequest request = new PecaUpdateRequest("Pastilha", "Pastilha freio", new BigDecimal("-1.00"), false);
        when(pecaRepository.findById(1L)).thenReturn(Optional.of(peca));

        assertThrows(ValidationException.class, () -> pecaService.atualizar(1L, request));

        verify(pecaRepository).findById(1L);
        verify(pecaRepository, never()).save(any(Peca.class));
    }

    @Test
    void deveAtualizarEstoqueComSucesso() {
        Peca peca = criarPeca(1L);
        AtualizarEstoqueRequest request = new AtualizarEstoqueRequest(50);
        when(pecaRepository.findById(1L)).thenReturn(Optional.of(peca));

        Peca resultado = pecaService.atualizarEstoque(1L, request);

        assertThat(resultado.getQuantidadeEstoque()).isEqualTo(50);
        verify(pecaRepository).findById(1L);
        verify(pecaRepository).save(any(Peca.class));
    }

    @Test
    void deveLancarValidationExceptionAoAtualizarEstoqueComQuantidadeNegativa() {
        Peca peca = criarPeca(1L);
        AtualizarEstoqueRequest request = new AtualizarEstoqueRequest(-1);
        when(pecaRepository.findById(1L)).thenReturn(Optional.of(peca));

        assertThrows(ValidationException.class, () -> pecaService.atualizarEstoque(1L, request));

        verify(pecaRepository).findById(1L);
        verify(pecaRepository, never()).save(any(Peca.class));
    }

    @Test
    void deveLancarResourceNotFoundExceptionAoAtualizarEstoqueDePecaInexistente() {
        AtualizarEstoqueRequest request = new AtualizarEstoqueRequest(10);
        when(pecaRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> pecaService.atualizarEstoque(1L, request));

        verify(pecaRepository).findById(1L);
        verify(pecaRepository, never()).save(any(Peca.class));
    }

    @Test
    void deveExcluirComSucesso() {
        Peca peca = criarPeca(1L);
        when(pecaRepository.findById(1L)).thenReturn(Optional.of(peca));

        pecaService.excluir(1L);

        verify(pecaRepository).findById(1L);
        verify(pecaRepository).delete(peca);
    }

    @Test
    void deveLancarResourceNotFoundExceptionAoExcluirPecaInexistente() {
        when(pecaRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> pecaService.excluir(1L));

        verify(pecaRepository).findById(1L);
        verify(pecaRepository, never()).delete(any(Peca.class));
    }

    private Peca criarPeca(Long id) {
        Peca peca = new Peca();
        peca.setId(id);
        peca.setNome("Filtro");
        peca.setDescricao("Filtro de oleo");
        peca.setValorUnitario(new BigDecimal("25.00"));
        peca.setQuantidadeEstoque(10);
        peca.setAtivo(true);
        return peca;
    }
}
