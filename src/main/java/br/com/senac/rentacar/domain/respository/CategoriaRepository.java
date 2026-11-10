package br.com.senac.rentacar.domain.respository;

import br.com.senac.rentacar.domain.entities.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    Optional<Categoria> findFirstByNomeIgnoreCase(String nome);
}
