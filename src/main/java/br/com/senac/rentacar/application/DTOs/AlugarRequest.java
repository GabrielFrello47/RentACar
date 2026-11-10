package br.com.senac.rentacar.application.DTOs;

import java.time.LocalDate;

/** Pedido de locacao feito pelo proprio cliente logado (ROLE_CLIENTE). */
public record AlugarRequest(Long veiculoId, LocalDate dataInicio, LocalDate dataFim) {
}
