package br.com.senac.rentacar.domain.respository;


import br.com.senac.rentacar.domain.entities.EnumStatusUsuario;
import br.com.senac.rentacar.domain.entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario,Long> {

    Optional<Usuario> findFirstByEmail(String email);

    boolean existsByEmail(String email);

    Optional<Usuario> findFirstByEmailIgnoreCase(String email);

    List<Usuario> findByStatusNot(EnumStatusUsuario status);
}
