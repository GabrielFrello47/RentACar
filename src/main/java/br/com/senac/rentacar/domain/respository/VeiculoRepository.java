package br.com.senac.rentacar.domain.respository;
import br.com.senac.rentacar.domain.entities.EnumStatusVeiculo;
import br.com.senac.rentacar.domain.entities.Veiculo;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VeiculoRepository extends JpaRepository<Veiculo, Long> {

    boolean existsByCategoria_Id(Long categoriaId);

    Optional<Veiculo> findFirstByPlacaIgnoreCase(String placa);

    List<Veiculo> findByStatus(EnumStatusVeiculo status);

    // trava a linha: duas locacoes simultaneas do mesmo veiculo nao passam juntas pela regra de disponibilidade
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select v from Veiculo v where v.id = :id")
    Optional<Veiculo> buscarParaAtualizar(@Param("id") Long id);
}
