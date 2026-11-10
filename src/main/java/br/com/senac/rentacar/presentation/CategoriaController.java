package br.com.senac.rentacar.presentation;

import br.com.senac.rentacar.domain.entities.Categoria;
import br.com.senac.rentacar.domain.respository.CategoriaRepository;
import br.com.senac.rentacar.domain.respository.VeiculoRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/categorias")
@Tag(name = "Categorias", description = "Categorias de veiculos (Economico, SUV, Utilitario...). Criar/alterar/excluir exige perfil ADMIN.")
public class CategoriaController {

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private VeiculoRepository veiculoRepository;

    @GetMapping
    @Operation(summary = "Listar categorias")
    public ResponseEntity<?> listarTodos() {
        return ResponseEntity.ok(categoriaRepository.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar categoria por id")
    public ResponseEntity<Categoria> buscarPorId(@PathVariable Long id) {
        return categoriaRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Criar categoria")
    public ResponseEntity<?> criar(@RequestBody Categoria categoria) {

        String erro = validar(categoria, null);
        if (erro != null) {
            return ResponseEntity.badRequest().body(erro);
        }

        Categoria nova = new Categoria();
        nova.setNome(categoria.getNome().trim());
        nova.setDescricao(categoria.getDescricao());
        return ResponseEntity.ok(categoriaRepository.save(nova));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar categoria")
    public ResponseEntity<?> atualizar(@PathVariable Long id, @RequestBody Categoria categoria) {

        Categoria banco = categoriaRepository.findById(id).orElse(null);
        if (banco == null) {
            return ResponseEntity.notFound().build();
        }

        String erro = validar(categoria, id);
        if (erro != null) {
            return ResponseEntity.badRequest().body(erro);
        }

        banco.setNome(categoria.getNome().trim());
        banco.setDescricao(categoria.getDescricao());
        return ResponseEntity.ok(categoriaRepository.save(banco));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir categoria (somente se nenhum veiculo a usa)")
    public ResponseEntity<?> excluir(@PathVariable Long id) {
        if (!categoriaRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        if (veiculoRepository.existsByCategoria_Id(id)) {
            return ResponseEntity.badRequest().body("Categoria em uso por veiculos: nao pode ser excluida!");
        }
        categoriaRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    private String validar(Categoria categoria, Long idAtual) {
        if (categoria == null || categoria.getNome() == null || categoria.getNome().isBlank()) {
            return "Informe o nome da categoria!";
        }
        var existente = categoriaRepository.findFirstByNomeIgnoreCase(categoria.getNome().trim()).orElse(null);
        if (existente != null && !existente.getId().equals(idAtual)) {
            return "Ja existe uma categoria com este nome!";
        }
        return null;
    }
}
