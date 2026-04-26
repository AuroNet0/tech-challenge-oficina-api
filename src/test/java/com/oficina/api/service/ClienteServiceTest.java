package com.oficina.api.service;

import com.oficina.api.dto.request.cliente.ClienteCreateRequest;
import com.oficina.api.dto.request.cliente.ClienteUpdateRequest;
import com.oficina.api.exception.BusinessException;
import com.oficina.api.exception.ResourceNotFoundException;
import com.oficina.api.exception.ValidationException;
import com.oficina.api.model.Cliente;
import com.oficina.api.model.enums.TipoPessoa;
import com.oficina.api.repository.ClienteRepository;
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
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private ClienteService clienteService;

    @BeforeEach
    void setUp() {
        lenient().when(clienteRepository.save(any(Cliente.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void deveCriarClienteComSucesso() {
        ClienteCreateRequest request = new ClienteCreateRequest(
                "Joao",
                "111.444.777-35",
                TipoPessoa.PF,
                "11999999999",
                "joao@email.com"
        );

        when(clienteRepository.existsByCpfCnpj("111.444.777-35")).thenReturn(false);
        when(clienteRepository.existsByEmail("joao@email.com")).thenReturn(false);

        Cliente resultado = clienteService.criar(request);

        assertThat(resultado.getNome()).isEqualTo("Joao");
        assertThat(resultado.getCpfCnpj()).isEqualTo("111.444.777-35");
        assertThat(resultado.getTipoPessoa()).isEqualTo(TipoPessoa.PF);
        assertThat(resultado.getTelefone()).isEqualTo("11999999999");
        assertThat(resultado.getEmail()).isEqualTo("joao@email.com");
        verify(clienteRepository).existsByCpfCnpj("111.444.777-35");
        verify(clienteRepository).existsByEmail("joao@email.com");
        verify(clienteRepository).save(any(Cliente.class));
    }

    @Test
    void deveLancarValidationExceptionAoCriarComCpfCnpjInvalido() {
        ClienteCreateRequest request = new ClienteCreateRequest(
                "Joao",
                "123",
                TipoPessoa.PF,
                "11999999999",
                "joao@email.com"
        );

        assertThrows(ValidationException.class, () -> clienteService.criar(request));

        verify(clienteRepository, never()).existsByCpfCnpj(any());
        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    @Test
    void deveLancarBusinessExceptionAoCriarComCpfCnpjDuplicado() {
        ClienteCreateRequest request = new ClienteCreateRequest(
                "Joao",
                "111.444.777-35",
                TipoPessoa.PF,
                "11999999999",
                "joao@email.com"
        );
        when(clienteRepository.existsByCpfCnpj("111.444.777-35")).thenReturn(true);

        assertThrows(BusinessException.class, () -> clienteService.criar(request));

        verify(clienteRepository).existsByCpfCnpj("111.444.777-35");
        verify(clienteRepository, never()).existsByEmail(any());
        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    @Test
    void deveLancarBusinessExceptionAoCriarComEmailDuplicado() {
        ClienteCreateRequest request = new ClienteCreateRequest(
                "Joao",
                "111.444.777-35",
                TipoPessoa.PF,
                "11999999999",
                "joao@email.com"
        );
        when(clienteRepository.existsByCpfCnpj("111.444.777-35")).thenReturn(false);
        when(clienteRepository.existsByEmail("joao@email.com")).thenReturn(true);

        assertThrows(BusinessException.class, () -> clienteService.criar(request));

        verify(clienteRepository).existsByCpfCnpj("111.444.777-35");
        verify(clienteRepository).existsByEmail("joao@email.com");
        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    @Test
    void deveBuscarPorIdComSucesso() {
        Cliente cliente = criarCliente(1L, "joao@email.com");
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));

        Cliente resultado = clienteService.buscarPorId(1L);

        assertThat(resultado).isEqualTo(cliente);
        verify(clienteRepository).findById(1L);
    }

    @Test
    void deveLancarResourceNotFoundExceptionAoBuscarIdInexistente() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> clienteService.buscarPorId(1L));

        verify(clienteRepository).findById(1L);
    }

    @Test
    void deveListarClientes() {
        when(clienteRepository.findAll()).thenReturn(List.of(
                criarCliente(1L, "a@email.com"),
                criarCliente(2L, "b@email.com")
        ));

        List<Cliente> resultado = clienteService.listar();

        assertThat(resultado).hasSize(2);
        verify(clienteRepository).findAll();
    }

    @Test
    void deveAtualizarComSucesso() {
        Cliente cliente = criarCliente(1L, "atual@email.com");
        ClienteUpdateRequest request = new ClienteUpdateRequest("Novo Nome", "11888887777", "novo@email.com");

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(clienteRepository.existsByEmail("novo@email.com")).thenReturn(false);

        Cliente resultado = clienteService.atualizar(1L, request);

        assertThat(resultado.getNome()).isEqualTo("Novo Nome");
        assertThat(resultado.getTelefone()).isEqualTo("11888887777");
        assertThat(resultado.getEmail()).isEqualTo("novo@email.com");
        assertThat(resultado.getCpfCnpj()).isEqualTo(cliente.getCpfCnpj());
        assertThat(resultado.getTipoPessoa()).isEqualTo(cliente.getTipoPessoa());
        verify(clienteRepository).findById(1L);
        verify(clienteRepository).existsByEmail("novo@email.com");
        verify(clienteRepository).save(any(Cliente.class));
    }

    @Test
    void deveLancarResourceNotFoundExceptionAoAtualizarClienteInexistente() {
        ClienteUpdateRequest request = new ClienteUpdateRequest("Novo Nome", "11888887777", "novo@email.com");
        when(clienteRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> clienteService.atualizar(1L, request));

        verify(clienteRepository).findById(1L);
        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    @Test
    void deveLancarBusinessExceptionAoAtualizarComEmailDuplicado() {
        Cliente cliente = criarCliente(1L, "atual@email.com");
        ClienteUpdateRequest request = new ClienteUpdateRequest("Novo Nome", "11888887777", "duplicado@email.com");

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(clienteRepository.existsByEmail("duplicado@email.com")).thenReturn(true);

        assertThrows(BusinessException.class, () -> clienteService.atualizar(1L, request));

        verify(clienteRepository).findById(1L);
        verify(clienteRepository).existsByEmail("duplicado@email.com");
        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    @Test
    void deveExcluirComSucesso() {
        Cliente cliente = criarCliente(1L, "excluir@email.com");
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));

        clienteService.excluir(1L);

        verify(clienteRepository).findById(1L);
        verify(clienteRepository).delete(cliente);
    }

    @Test
    void deveLancarResourceNotFoundExceptionAoExcluirClienteInexistente() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> clienteService.excluir(1L));

        verify(clienteRepository).findById(1L);
        verify(clienteRepository, never()).delete(any(Cliente.class));
    }

    private Cliente criarCliente(Long id, String email) {
        Cliente cliente = new Cliente();
        cliente.setId(id);
        cliente.setNome("Cliente " + id);
        cliente.setCpfCnpj("111.444.777-35");
        cliente.setTipoPessoa(TipoPessoa.PF);
        cliente.setTelefone("11999999999");
        cliente.setEmail(email);
        return cliente;
    }
}
