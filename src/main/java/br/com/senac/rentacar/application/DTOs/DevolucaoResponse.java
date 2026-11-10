package br.com.senac.rentacar.application.DTOs;

/**
 * Resultado da devolucao. Se alertaRevisao for true, o veiculo passou do limite de quilometragem
 * desde a ultima revisao e a tela deve avisar o atendente.
 */
public record DevolucaoResponse(Long locacaoId, Long veiculoId, Integer quilometragemAtual,
                                int kmDesdeUltimaRevisao, int limiteRevisaoKm,
                                boolean alertaRevisao, String mensagem) {
}
