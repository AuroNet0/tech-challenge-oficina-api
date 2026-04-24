package com.oficina.api.dto.request.cliente;

import com.oficina.api.model.enums.TipoPessoa;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ClienteCreateRequest(
        @NotBlank(message = "Nome e obrigatorio.")
        @Size(max = 120, message = "Nome deve ter no maximo 120 caracteres.")
        String nome,

        @NotBlank(message = "CPF/CNPJ e obrigatorio.")
        @Size(min = 11, max = 18, message = "CPF/CNPJ deve ter entre 11 e 18 caracteres.")
        String cpfCnpj,

        @NotNull(message = "Tipo de pessoa e obrigatorio.")
        TipoPessoa tipoPessoa,

        @NotBlank(message = "Telefone e obrigatorio.")
        @Size(max = 20, message = "Telefone deve ter no maximo 20 caracteres.")
        String telefone,

        @Email(message = "Email invalido.")
        @Size(max = 120, message = "Email deve ter no maximo 120 caracteres.")
        String email
) {
}
