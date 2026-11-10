package br.com.senac.rentacar.domain.respository;


import br.com.senac.rentacar.domain.entities.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    java.util.Optional<Cliente> findFirstByEmailIgnoreCase(String email);

}