package com.oficina.api.service;

import com.oficina.api.dto.request.veiculo.VeiculoCreateRequest;
import com.oficina.api.dto.request.veiculo.VeiculoUpdateRequest;
import com.oficina.api.exception.BusinessException;
import com.oficina.api.exception.ResourceNotFoundException;
import com.oficina.api.exception.ValidationException;
import com.oficina.api.model.Cliente;
import com.oficina.api.model.Veiculo;
import com.oficina.api.model.enums.TipoPessoa;
import com.oficina.api.repository.ClienteRepository;
import com.oficina.api.repository.VeiculoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
class VeiculoServiceTest {

    @Mock
    private VeiculoRepository veiculoRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private VeiculoService veiculoService;

    @BeforeEach
    void setUp() {
        lenient().when(veiculoRepository.save(any(Veiculo.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void deveCriarVeiculoComSucesso() {
        Cliente cliente = criarCliente(1L);
        VeiculoCreateRequest request = new VeiculoCreateRequest("ABC1D23", "VW", "Gol", 2020, 1L);

        when(veiculoRepository.existsByPlaca("ABC1D23")).thenReturn(false);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));

        Veiculo resultado = veiculoService.criar(request);

        assertThat(resultado.getPlaca()).isEqualTo("ABC1D23");
        assertThat(resultado.getMarca()).isEqualTo("VW");
        assertThat(resultado.getModelo()).isEqualTo("Gol");
        assertThat(resultado.getAno()).isEqualTo(2020);
        assertThat(resultado.getCliente()).isEqualTo(cliente);
        verify(veiculoRepository).existsByPlaca("ABC1D23");
        verify(clienteRepository).findById(1L);
        verify(veiculoRepository).save(any(Veiculo.class));
    }

    @Test
    void deveLancarValidationExceptionAoCriarComPlacaInvalida() {
        VeiculoCreateRequest request = new VeiculoCreateRequest("123", "VW", "Gol", 2020, 1L);

        assertThrows(ValidationException.class, () -> veiculoService.criar(request));

        verify(veiculoRepository, never()).existsByPlaca(any());
        verify(clienteRepository, never()).findById(any());
        verify(veiculoRepository, never()).save(any(Veiculo.class));
    }

    @Test
    void deveLancarBusinessExceptionAoCriarComPlacaDuplicada() {
        VeiculoCreateRequest request = new VeiculoCreateRequest("ABC1D23", "VW", "Gol", 2020, 1L);
        when(veiculoRepository.existsByPlaca("ABC1D23")).thenReturn(true);

        assertThrows(BusinessException.class, () -> veiculoService.criar(request));

        verify(veiculoRepository).existsByPlaca("ABC1D23");
        verify(clienteRepository, never()).findById(any());
        verify(veiculoRepository, never()).save(any(Veiculo.class));
    }

    @Test
    void deveLancarResourceNotFoundExceptionAoCriarComClienteInexistente() {
        VeiculoCreateRequest request = new VeiculoCreateRequest("ABC1D23", "VW", "Gol", 2020, 1L);
        when(veiculoRepository.existsByPlaca("ABC1D23")).thenReturn(false);
        when(clienteRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> veiculoService.criar(request));

        verify(veiculoRepository).existsByPlaca("ABC1D23");
        verify(clienteRepository).findById(1L);
        verify(veiculoRepository, never()).save(any(Veiculo.class));
    }

    @Test
    void deveBuscarPorIdComSucesso() {
        Veiculo veiculo = criarVeiculo(10L, criarCliente(1L));
        when(veiculoRepository.findById(10L)).thenReturn(Optional.of(veiculo));

        Veiculo resultado = veiculoService.buscarPorId(10L);

        assertThat(resultado).isEqualTo(veiculo);
        verify(veiculoRepository).findById(10L);
    }

    @Test
    void deveLancarResourceNotFoundExceptionAoBuscarIdInexistente() {
        when(veiculoRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> veiculoService.buscarPorId(10L));

        verify(veiculoRepository).findById(10L);
    }

    @Test
    void deveListarVeiculos() {
        when(veiculoRepository.findAll()).thenReturn(List.of(
                criarVeiculo(10L, criarCliente(1L)),
                criarVeiculo(11L, criarCliente(2L))
        ));

        List<Veiculo> resultado = veiculoService.listar();

        assertThat(resultado).hasSize(2);
        verify(veiculoRepository).findAll();
    }

    @Test
    void deveListarVeiculosPorClienteComSucesso() {
        Cliente cliente = criarCliente(1L);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(veiculoRepository.findByClienteId(1L)).thenReturn(List.of(
                criarVeiculo(10L, cliente),
                criarVeiculo(11L, cliente)
        ));

        List<Veiculo> resultado = veiculoService.listarPorCliente(1L);

        assertThat(resultado).hasSize(2);
        verify(clienteRepository).findById(1L);
        verify(veiculoRepository).findByClienteId(1L);
    }

    @Test
    void deveLancarResourceNotFoundExceptionAoListarPorClienteInexistente() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> veiculoService.listarPorCliente(1L));

        verify(clienteRepository).findById(1L);
        verify(veiculoRepository, never()).findByClienteId(any());
    }

    @Test
    void deveAtualizarComSucesso() {
        Cliente cliente = criarCliente(1L);
        Veiculo veiculo = criarVeiculo(10L, cliente);
        VeiculoUpdateRequest request = new VeiculoUpdateRequest("Fiat", "Argo", 2022);

        when(veiculoRepository.findById(10L)).thenReturn(Optional.of(veiculo));

        Veiculo resultado = veiculoService.atualizar(10L, request);

        assertThat(resultado.getMarca()).isEqualTo("Fiat");
        assertThat(resultado.getModelo()).isEqualTo("Argo");
        assertThat(resultado.getAno()).isEqualTo(2022);
        assertThat(resultado.getPlaca()).isEqualTo("ABC1D23");
        assertThat(resultado.getCliente()).isEqualTo(cliente);
        verify(veiculoRepository).findById(10L);
        verify(veiculoRepository).save(any(Veiculo.class));
    }

    @Test
    void deveLancarResourceNotFoundExceptionAoAtualizarVeiculoInexistente() {
        VeiculoUpdateRequest request = new VeiculoUpdateRequest("Fiat", "Argo", 2022);
        when(veiculoRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> veiculoService.atualizar(10L, request));

        verify(veiculoRepository).findById(10L);
        verify(veiculoRepository, never()).save(any(Veiculo.class));
    }

    @Test
    void deveExcluirComSucesso() {
        Veiculo veiculo = criarVeiculo(10L, criarCliente(1L));
        when(veiculoRepository.findById(10L)).thenReturn(Optional.of(veiculo));

        veiculoService.excluir(10L);

        verify(veiculoRepository).findById(10L);
        verify(veiculoRepository).delete(veiculo);
    }

    @Test
    void deveLancarResourceNotFoundExceptionAoExcluirVeiculoInexistente() {
        when(veiculoRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> veiculoService.excluir(10L));

        verify(veiculoRepository).findById(10L);
        verify(veiculoRepository, never()).delete(any(Veiculo.class));
    }

    private Cliente criarCliente(Long id) {
        Cliente cliente = new Cliente();
        cliente.setId(id);
        cliente.setNome("Cliente " + id);
        cliente.setCpfCnpj("111.444.777-35");
        cliente.setTipoPessoa(TipoPessoa.PF);
        cliente.setTelefone("11999999999");
        return cliente;
    }

    private Veiculo criarVeiculo(Long id, Cliente cliente) {
        Veiculo veiculo = new Veiculo();
        veiculo.setId(id);
        veiculo.setPlaca("ABC1D23");
        veiculo.setMarca("VW");
        veiculo.setModelo("Gol");
        veiculo.setAno(2020);
        veiculo.setCliente(cliente);
        return veiculo;
    }
}
