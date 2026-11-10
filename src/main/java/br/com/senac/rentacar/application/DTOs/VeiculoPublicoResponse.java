package br.com.senac.rentacar.application.DTOs;

import br.com.senac.rentacar.domain.entities.Veiculo;

/** Dados do veiculo que podem aparecer na vitrine publica (sem placa, quilometragem ou revisao). */
public record VeiculoPublicoResponse(Long id, String marca, String modelo, String ano, Double valorDiaria,
                                     String categoriaNome, String categoriaDescricao) {

    public VeiculoPublicoResponse(Veiculo v) {
        this(v.getId(), v.getMarca(), v.getModelo(), v.getAno(), v.getValorDiaria(),
                v.getCategoria() != null ? v.getCategoria().getNome() : null,
                v.getCategoria() != null ? v.getCategoria().getDescricao() : null);
    }
}
