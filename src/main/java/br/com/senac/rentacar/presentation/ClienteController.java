package br.com.senac.rentacar.presentation;

import br.com.senac.rentacar.domain.entities.Cliente;
import br.com.senac.rentacar.domain.respository.ClienteRepository;
import br.com.senac.rentacar.domain.valueobject.CPF;
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

    @Autowired
    private br.com.senac.rentacar.domain.respository.LocacaoRepository locacaoRepository;

    @Autowired
    private br.com.senac.rentacar.application.services.CpfDuplicidade cpfDuplicidade;

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
    public ResponseEntity<?> criar(@RequestBody Cliente cliente){

        String erro = validar(cliente, null);
        if (erro != null) {
            return ResponseEntity.badRequest().body(erro);
        }

        // o id sempre e gerado pelo banco: ignora qualquer id enviado no corpo
        cliente.setId(null);
        var clienteBanco = clienteRepository.save(cliente);
        return ResponseEntity.ok(clienteBanco);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Metodo de atualizacao de clientes", description = "Metodo responsavel por atualizar os dados de um cliente existente!")
    public ResponseEntity<?> atualizar(@PathVariable Long id, @RequestBody Cliente cliente) {

        String erro = validar(cliente, id);
        if (erro != null) {
            return ResponseEntity.badRequest().body(erro);
        }

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
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Metodo de exclusao de clientes", description = "Metodo responsavel por excluir um cliente existente!")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        if (!clienteRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        if (locacaoRepository.existsByClienteAndStatus(String.valueOf(id),
                br.com.senac.rentacar.domain.entities.EnumStatusLocacao.EM_ANDAMENTO)) {
            // antes a exclusao passava e deixava a locacao orfa com o veiculo preso como Alugado
            return ResponseEntity.badRequest().build();
        }
        clienteRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    /** @return a mensagem de erro, ou null se o cliente e valido. */
    private String validar(Cliente cliente, Long idAtual) {
        if (cliente == null || cliente.getNome() == null || cliente.getNome().isBlank()) {
            return "Informe o nome do cliente!";
        }
        if (!CPF.isValidCpf(cliente.getCpf())) {
            return "CPF Invalido!";
        }
        if (cpfDuplicidade.clienteJaTem(cliente.getCpf(), idAtual)) {
            return "Ja existe um cliente com este CPF!";
        }
        if (cliente.getEmail() != null && !cliente.getEmail().isBlank() && !cliente.getEmail().contains("@")) {
            return "Email Invalido!";
        }
        return null;
    }

}
