package br.com.senac.rentacar.presentation;

import br.com.senac.rentacar.application.DTOs.VeiculoRequest;
import br.com.senac.rentacar.application.DTOs.VeiculoResponse;
import br.com.senac.rentacar.application.services.VeiculoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/veiculos")
@Tag(name = "Veiculos", description = "Grupo de APIs responsavel por controlar a estrutura de veiculos do sistema")
public class VeiculoController {

    @Autowired
    private VeiculoService veiculoService;

    @GetMapping
    @Operation(summary = "Metodo de listagem de veiculos",
            description = "Lista os veiculos. Com ?disponiveis=true devolve so os que podem ser selecionados para nova locacao.")
    public ResponseEntity<List<VeiculoResponse>> listarTodos(@RequestParam(defaultValue = "false") boolean disponiveis){

        return ResponseEntity.ok(veiculoService.listar(disponiveis));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Metodo de busca de veiculo por id", description = "Metodo responsavel por buscar um veiculo especifico atraves do seu id!")
    public ResponseEntity<VeiculoResponse> buscarPorId(@PathVariable Long id){

        VeiculoResponse veiculo = veiculoService.buscar(id);
        if(veiculo != null){
            return ResponseEntity.ok(veiculo);
        }

        return ResponseEntity.notFound().build();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Metodo de criar de veiculos", description = "Metodo responsavel de criação de novos veiculos!")
    public ResponseEntity<?> criar(@RequestBody VeiculoRequest veiculo){

        try {
            return ResponseEntity.ok(veiculoService.criar(veiculo));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Metodo de atualizacao de veiculos", description = "Metodo responsavel por atualizar os dados de um veiculo existente!")
    public ResponseEntity<?> atualizar(@PathVariable Long id, @RequestBody VeiculoRequest veiculo) {

        try {
            VeiculoResponse atualizado = veiculoService.atualizar(id, veiculo);
            if (atualizado == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(atualizado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/revisao")
    @Operation(summary = "Registrar revisao feita",
            description = "Marca a quilometragem atual como a da ultima revisao (zera o alerta de revisao).")
    public ResponseEntity<?> registrarRevisao(@PathVariable Long id) {

        VeiculoResponse veiculo = veiculoService.registrarRevisao(id);
        if (veiculo == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(veiculo);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Metodo de exclusao de veiculos", description = "Metodo responsavel por excluir um veiculo existente!")
    public ResponseEntity<?> excluir(@PathVariable Long id) {
        try {
            if (!veiculoService.excluir(id)) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
