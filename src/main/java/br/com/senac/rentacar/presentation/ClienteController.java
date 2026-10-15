package br.com.senac.rentacar.presentation;

import br.com.senac.rentacar.domain.entities.Cliente;
import br.com.senac.rentacar.domain.respository.ClienteRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/clientes")
@Tag(name = "Clientes", description = "Grupo de APIs responsavel por controlar a estrutura de clientes do sistema")
public class ClienteController {

    @Autowired
    private ClienteRepository clienteRepository;

    @GetMapping
    @Operation(summary = "Metodo de listagem de clientes", description = "Metodo responsavel por listar todos os clientes cadastrados!")
    public ResponseEntity<?> listarTodos(){

        return ResponseEntity.ok(clienteRepository.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Metodo de busca de cliente por id", description = "Metodo responsavel por buscar um cliente especifico atraves do seu id!")
    public ResponseEntity<Cliente> buscarPorId(@PathVariable Long id){

        Cliente clienteBanco = clienteRepository.findById(id).orElse(null);
        if(clienteBanco != null){
            return ResponseEntity.ok(clienteBanco);
        }

        return ResponseEntity.notFound().build();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Metodo de criar de clientes", description = "Metodo responsavel de criação de novos clientes!")
    public ResponseEntity<Cliente> criar(@RequestBody Cliente cliente){

        var clienteBanco = clienteRepository.save(cliente);
        return ResponseEntity.ok(clienteBanco);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Metodo de atualizacao de clientes", description = "Metodo responsavel por atualizar os dados de um cliente existente!")
    public ResponseEntity<Cliente> atualizar(@PathVariable Long id, @RequestBody Cliente cliente) {
        try {
            Cliente clienteBanco = clienteRepository.findById(id).orElse(null);

            if (clienteBanco != null) {
                clienteBanco.setNome(cliente.getNome());
                clienteBanco.setCpf(cliente.getCpf());
                clienteBanco.setTelefone(cliente.getTelefone());
                clienteBanco.setEmail(cliente.getEmail());
                clienteRepository.save(clienteBanco);
                return ResponseEntity.ok().build();
            }

            return ResponseEntity.notFound().build();

        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Metodo de exclusao de clientes", description = "Metodo responsavel por excluir um cliente existente!")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        clienteRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

}