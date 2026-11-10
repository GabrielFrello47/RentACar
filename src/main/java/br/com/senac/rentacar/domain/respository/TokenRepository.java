package br.com.senac.rentacar.domain.respository;

import br.com.senac.rentacar.domain.entities.Token;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {

    Optional<Token> findTokenByToken(String token);

    void deleteByDataCadastroBefore(LocalDateTime data);
}
