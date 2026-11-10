package br.com.senac.rentacar.domain.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Veiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public String marca;

    public String modelo;

    public String placa;

    public String ano;

    public Double valorDiaria;

    // Regra central: veiculo EM_MANUTENCAO ou ALUGADO nao pode ser selecionado para nova locacao
    @Enumerated(EnumType.STRING)
    public EnumStatusVeiculo status = EnumStatusVeiculo.DISPONIVEL;

    // quilometragem atual do odometro
    public Integer quilometragem = 0;

    // quilometragem em que foi feita a ultima revisao (base do alerta de revisao)
    public Integer kmUltimaRevisao = 0;

    @ManyToOne
    @JoinColumn(name = "categoria_id")
    public Categoria categoria;

    // veiculos cadastrados antes destas colunas existirem vem com null do banco
    public EnumStatusVeiculo getStatus() {
        return status == null ? EnumStatusVeiculo.DISPONIVEL : status;
    }

    public Integer getQuilometragem() {
        return quilometragem == null ? 0 : quilometragem;
    }

    public Integer getKmUltimaRevisao() {
        return kmUltimaRevisao == null ? 0 : kmUltimaRevisao;
    }
}
