package com.oficina.api.controller;

import com.oficina.api.dto.request.ordemservico.AdicionarPecaOrdemRequest;
import com.oficina.api.dto.request.ordemservico.AdicionarServicoOrdemRequest;
import com.oficina.api.dto.request.ordemservico.AprovarOrcamentoRequest;
import com.oficina.api.dto.request.ordemservico.AtualizarStatusOrdemServicoRequest;
import com.oficina.api.dto.request.ordemservico.CriarOrdemServicoRequest;
import com.oficina.api.dto.response.ordemservico.ItemPecaOrdemResponse;
import com.oficina.api.dto.response.ordemservico.ItemServicoOrdemResponse;
import com.oficina.api.dto.response.ordemservico.OrcamentoResponse;
import com.oficina.api.dto.response.ordemservico.OrdemServicoResponse;
import com.oficina.api.dto.response.ordemservico.OrdemServicoResumoResponse;
import com.oficina.api.model.ItemPecaOrdem;
import com.oficina.api.model.ItemServicoOrdem;
import com.oficina.api.model.OrdemServico;
import com.oficina.api.model.enums.StatusOrdemServico;
import com.oficina.api.service.OrdemServicoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@RestController
@Validated
@RequestMapping("/ordens-servico")
public class OrdemServicoController {

    private final OrdemServicoService ordemServicoService;

    public OrdemServicoController(OrdemServicoService ordemServicoService) {
        this.ordemServicoService = ordemServicoService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrdemServicoResponse> buscarPorId(@PathVariable Long id) {
        OrdemServico ordemServico = ordemServicoService.buscarPorId(id);
        return ResponseEntity.ok(toResponse(ordemServico));
    }

    @GetMapping
    public ResponseEntity<List<OrdemServicoResumoResponse>> listar() {
        List<OrdemServico> ordens = ordemServicoService.listar();
        return ResponseEntity.ok(toResumoResponseList(ordens));
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<OrdemServicoResumoResponse>> listarPorCliente(@PathVariable Long clienteId) {
        List<OrdemServico> ordens = ordemServicoService.listarPorCliente(clienteId);
        return ResponseEntity.ok(toResumoResponseList(ordens));
    }

    @GetMapping("/veiculo/{veiculoId}")
    public ResponseEntity<List<OrdemServicoResumoResponse>> listarPorVeiculo(@PathVariable Long veiculoId) {
        List<OrdemServico> ordens = ordemServicoService.listarPorVeiculo(veiculoId);
        return ResponseEntity.ok(toResumoResponseList(ordens));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrdemServicoResumoResponse>> listarPorStatus(@PathVariable StatusOrdemServico status) {
        List<OrdemServico> ordens = ordemServicoService.listarPorStatus(status);
        return ResponseEntity.ok(toResumoResponseList(ordens));
    }

    @PostMapping
    public ResponseEntity<OrdemServicoResponse> criarOrdemServico(@Valid @RequestBody CriarOrdemServicoRequest request) {
        OrdemServico ordemServico = ordemServicoService.criarOrdemServico(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(ordemServico));
    }

    @PostMapping("/{id}/servicos")
    public ResponseEntity<OrdemServicoResponse> adicionarServico(@PathVariable Long id,
                                                                 @Valid @RequestBody AdicionarServicoOrdemRequest request) {
        OrdemServico ordemServico = ordemServicoService.adicionarServico(id, request);
        return ResponseEntity.ok(toResponse(ordemServico));
    }

    @PostMapping("/{id}/pecas")
    public ResponseEntity<OrdemServicoResponse> adicionarPeca(@PathVariable Long id,
                                                              @Valid @RequestBody AdicionarPecaOrdemRequest request) {
        OrdemServico ordemServico = ordemServicoService.adicionarPeca(id, request);
        return ResponseEntity.ok(toResponse(ordemServico));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<OrdemServicoResponse> atualizarStatus(@PathVariable Long id,
                                                                @Valid @RequestBody AtualizarStatusOrdemServicoRequest request) {
        OrdemServico ordemServico = ordemServicoService.atualizarStatus(id, request);
        return ResponseEntity.ok(toResponse(ordemServico));
    }

    @PatchMapping("/{id}/orcamento")
    public ResponseEntity<OrdemServicoResponse> aprovarOrcamento(@PathVariable Long id,
                                                                 @Valid @RequestBody AprovarOrcamentoRequest request) {
        OrdemServico ordemServico = ordemServicoService.aprovarOrcamento(id, request);
        return ResponseEntity.ok(toResponse(ordemServico));
    }

    private OrdemServicoResponse toResponse(OrdemServico ordemServico) {
        List<ItemServicoOrdemResponse> itensServico = toItensServicoResponse(ordemServico.getItensServico());
        List<ItemPecaOrdemResponse> itensPeca = toItensPecaResponse(ordemServico.getItensPeca());

        BigDecimal valorTotal = nullSafe(ordemServico.getValorTotal());
        BigDecimal valorServicos = calcularValorServicos(ordemServico.getItensServico());
        BigDecimal valorPecas = calcularValorPecas(ordemServico.getItensPeca());

        return new OrdemServicoResponse(
                ordemServico.getId(),
                ordemServico.getCliente() != null ? ordemServico.getCliente().getId() : null,
                ordemServico.getCliente() != null ? ordemServico.getCliente().getNome() : null,
                ordemServico.getVeiculo() != null ? ordemServico.getVeiculo().getId() : null,
                ordemServico.getVeiculo() != null ? ordemServico.getVeiculo().getPlaca() : null,
                ordemServico.getStatus(),
                ordemServico.getDataAbertura(),
                ordemServico.getDataFinalizacao(),
                valorTotal,
                ordemServico.getObservacoes(),
                itensServico,
                itensPeca,
                new OrcamentoResponse(valorServicos, valorPecas, valorTotal)
        );
    }

    private OrdemServicoResumoResponse toResumoResponse(OrdemServico ordemServico) {
        return new OrdemServicoResumoResponse(
                ordemServico.getId(),
                ordemServico.getCliente() != null ? ordemServico.getCliente().getNome() : null,
                ordemServico.getVeiculo() != null ? ordemServico.getVeiculo().getPlaca() : null,
                ordemServico.getStatus(),
                ordemServico.getDataAbertura(),
                nullSafe(ordemServico.getValorTotal())
        );
    }

    private List<OrdemServicoResumoResponse> toResumoResponseList(List<OrdemServico> ordens) {
        List<OrdemServico> lista = ordens == null ? Collections.emptyList() : ordens;
        return lista.stream()
                .map(this::toResumoResponse)
                .toList();
    }

    private BigDecimal calcularValorServicos(List<ItemServicoOrdem> itensServico) {
        List<ItemServicoOrdem> itens = itensServico == null ? Collections.emptyList() : itensServico;
        return itens.stream()
                .map(item -> nullSafe(item.getValor()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calcularValorPecas(List<ItemPecaOrdem> itensPeca) {
        List<ItemPecaOrdem> itens = itensPeca == null ? Collections.emptyList() : itensPeca;
        return itens.stream()
                .map(item -> {
                    BigDecimal valorUnitario = nullSafe(item.getValorUnitario());
                    Integer quantidade = item.getQuantidade() == null ? 0 : item.getQuantidade();
                    return valorUnitario.multiply(BigDecimal.valueOf(quantidade));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<ItemServicoOrdemResponse> toItensServicoResponse(List<ItemServicoOrdem> itensServico) {
        List<ItemServicoOrdem> itens = itensServico == null ? Collections.emptyList() : itensServico;
        return itens.stream()
                .map(item -> new ItemServicoOrdemResponse(
                        item.getId(),
                        item.getServico() != null ? item.getServico().getId() : null,
                        item.getServico() != null ? item.getServico().getDescricao() : null,
                        nullSafe(item.getValor()),
                        item.getObservacao()
                ))
                .toList();
    }

    private List<ItemPecaOrdemResponse> toItensPecaResponse(List<ItemPecaOrdem> itensPeca) {
        List<ItemPecaOrdem> itens = itensPeca == null ? Collections.emptyList() : itensPeca;
        return itens.stream()
                .map(item -> {
                    BigDecimal valorUnitario = nullSafe(item.getValorUnitario());
                    Integer quantidade = item.getQuantidade() == null ? 0 : item.getQuantidade();
                    BigDecimal valorTotal = valorUnitario.multiply(BigDecimal.valueOf(quantidade));
                    return new ItemPecaOrdemResponse(
                            item.getId(),
                            item.getPeca() != null ? item.getPeca().getId() : null,
                            item.getPeca() != null ? item.getPeca().getNome() : null,
                            item.getQuantidade(),
                            valorUnitario,
                            valorTotal
                    );
                })
                .toList();
    }

    private BigDecimal nullSafe(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }
}
