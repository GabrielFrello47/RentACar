package br.com.senac.rentacar.application.DTOs;

import br.com.senac.rentacar.domain.entities.EnumStatusVeiculo;

public record VeiculoRequest(String marca, String modelo, String placa, String ano, Double valorDiaria,
                             EnumStatusVeiculo status, Integer quilometragem, Integer kmUltimaRevisao,
                             Long categoriaId) {
}
