package br.com.senac.rentacar.application.DTOs;

import br.com.senac.rentacar.domain.entities.EnumStatusVeiculo;
import br.com.senac.rentacar.domain.entities.Veiculo;

public record VeiculoResponse(Long id, String marca, String modelo, String placa, String ano, Double valorDiaria,
                              EnumStatusVeiculo status, Integer quilometragem, Integer kmUltimaRevisao,
                              Long categoriaId, String categoriaNome,
                              boolean revisaoPendente, int limiteRevisaoKm) {

    public VeiculoResponse(Veiculo v, int limiteRevisaoKm) {
        this(v.getId(), v.getMarca(), v.getModelo(), v.getPlaca(), v.getAno(), v.getValorDiaria(),
                v.getStatus(), v.getQuilometragem(), v.getKmUltimaRevisao(),
                v.getCategoria() != null ? v.getCategoria().getId() : null,
                v.getCategoria() != null ? v.getCategoria().getNome() : null,
                v.getQuilometragem() - v.getKmUltimaRevisao() >= limiteRevisaoKm,
                limiteRevisaoKm);
    }
}
