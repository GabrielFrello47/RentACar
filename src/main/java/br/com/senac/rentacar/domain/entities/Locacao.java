package br.com.senac.rentacar.domain.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Locacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public LocalDate dataInicio;

    public LocalDate dataFim;

    public Double valorTotal;

    // id do cliente / id do veiculo (mantido como texto, como no projeto original)
    public String cliente;

    public String veiculo;

    // definido pelo sistema (null = locacao antiga, anterior ao controle de status)
    @Enumerated(EnumType.STRING)
    public EnumStatusLocacao status;

    public LocalDate dataDevolucao;

    public Integer quilometragemDevolucao;

    public boolean emAndamento() {
        return status == EnumStatusLocacao.EM_ANDAMENTO;
    }
}
