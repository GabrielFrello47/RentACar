package br.com.senac.rentacar.presentation;

import br.com.senac.rentacar.application.DTOs.VeiculoPublicoResponse;
import br.com.senac.rentacar.domain.entities.EnumStatusVeiculo;
import br.com.senac.rentacar.domain.respository.VeiculoRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/publico/veiculos")
@Tag(name = "Vitrine publica", description = "Veiculos disponiveis para a landing page. Nao exige login.")
public class VitrineController {

    @Autowired
    private VeiculoRepository veiculoRepository;

    @Autowired
    private br.com.senac.rentacar.application.services.VeiculoService veiculoService;

    @GetMapping
    @Operation(summary = "Listar veiculos disponiveis (publico)",
            description = "So veiculos DISPONIVEL; nao devolve placa nem quilometragem.")
    public ResponseEntity<List<VeiculoPublicoResponse>> listar() {
        return ResponseEntity.ok(veiculoRepository.findByStatus(EnumStatusVeiculo.DISPONIVEL).stream()
                .filter(v -> !veiculoService.revisaoPendente(v))
                .map(VeiculoPublicoResponse::new)
                .toList());
    }
}
