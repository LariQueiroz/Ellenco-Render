package com.ellenco.ellenco.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ObraRequest(
        @NotBlank(message = "Informe o nome da obra.")
        @Size(max = 255, message = "O nome da obra deve ter no máximo 255 caracteres.")
        String nome,

        @Size(max = 255, message = "A localização deve ter no máximo 255 caracteres.")
        String localizacao,

        @Size(max = 255, message = "O responsável deve ter no máximo 255 caracteres.")
        String responsavel,

        @Size(max = 5000, message = "A descrição é muito longa.")
        String descricao,

        LocalDate dataInicio,

        LocalDate previsaoTermino) {
}
