package br.com.senac.rentacar.presentation;

import br.com.senac.rentacar.application.DTOs.DevolucaoRequest;
import br.com.senac.rentacar.application.DTOs.DevolucaoResponse;
import br.com.senac.rentacar.application.services.LocacaoService;
import br.com.senac.rentacar.domain.entities.Locacao;
import br.com.senac.rentacar.domain.respository.LocacaoRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/locacoes")
@Tag(name = "Locacoes", description = "Grupo de APIs responsavel por controlar a estrutura de locacoes do sistema")
public class LocacaoController {

    @Autowired
    private LocacaoRepository locacaoRepository;

    @Autowired
    private LocacaoService locacaoService;

    @GetMapping
    @Operation(summary = "Metodo de listagem de locacoes", description = "Metodo responsavel por listar todas as locacoes cadastradas!")
    public ResponseEntity<?> listarTodos(){

        return ResponseEntity.ok(locacaoRepository.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Metodo de busca de locacao por id", description = "Metodo responsavel por buscar uma locacao especifica atraves do seu id!")
    public ResponseEntity<Locacao> buscarPorId(@PathVariable Long id){

        Locacao locacaoBanco = locacaoRepository.findById(id).orElse(null);
        if(locacaoBanco != null){
            return ResponseEntity.ok(locacaoBanco);
        }

        return ResponseEntity.notFound().build();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Metodo de criar de locacoes",
            description = "Cria a locacao. Veiculos Em Manutencao ou Alugados sao recusados (400).")
    public ResponseEntity<?> criar(@RequestBody Locacao locacao){

        try {
            return ResponseEntity.ok(locacaoService.criar(locacao));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Metodo de atualizacao de locacoes", description = "Metodo responsavel por atualizar os dados de uma locacao existente!")
    public ResponseEntity<?> atualizar(@PathVariable Long id, @RequestBody Locacao locacao) {

        try {
            Locacao atualizada = locacaoService.atualizar(id, locacao);
            if (atualizada == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/devolucao")
    @Operation(summary = "Registrar devolucao",
            description = "Finaliza a locacao, libera o veiculo e atualiza a quilometragem. "
                    + "Se o veiculo passou do limite de km desde a ultima revisao, devolve alertaRevisao=true.")
    public ResponseEntity<?> devolver(@PathVariable Long id, @RequestBody DevolucaoRequest request) {

        try {
            DevolucaoResponse resposta = locacaoService.devolver(id, request.quilometragemFinal());
            if (resposta == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(resposta);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Metodo de exclusao de locacoes", description = "Metodo responsavel por excluir uma locacao existente!")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        if (!locacaoService.excluir(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().build();
    }

}
