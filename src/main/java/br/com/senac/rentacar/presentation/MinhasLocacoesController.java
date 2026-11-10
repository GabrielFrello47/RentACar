package br.com.senac.rentacar.presentation;

import br.com.senac.rentacar.application.DTOs.AlugarRequest;
import br.com.senac.rentacar.application.services.LocacaoService;
import br.com.senac.rentacar.application.services.UsuarioService;
import br.com.senac.rentacar.domain.entities.Cliente;
import br.com.senac.rentacar.domain.entities.Locacao;
import br.com.senac.rentacar.domain.entities.Veiculo;
import br.com.senac.rentacar.domain.respository.ClienteRepository;
import br.com.senac.rentacar.domain.respository.LocacaoRepository;
import br.com.senac.rentacar.domain.respository.VeiculoRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** Area do cliente logado (ROLE_CLIENTE): pedir uma locacao e ver as proprias. */
@RestController
@RequestMapping("/minhas-locacoes")
@Tag(name = "Minhas locacoes", description = "Locacoes do proprio cliente logado (ROLE_CLIENTE).")
public class MinhasLocacoesController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private VeiculoRepository veiculoRepository;

    @Autowired
    private LocacaoRepository locacaoRepository;

    @Autowired
    private LocacaoService locacaoService;

    @GetMapping
    @Operation(summary = "Listar as minhas locacoes")
    public ResponseEntity<?> listar() {
        Cliente cliente = clienteLogado();
        if (cliente == null) {
            return ResponseEntity.ok(java.util.List.of());
        }
        return ResponseEntity.ok(locacaoRepository.findByClienteOrderByIdDesc(String.valueOf(cliente.getId())));
    }

    @PostMapping
    @Operation(summary = "Alugar um veiculo",
            description = "O valor e calculado pelo servidor (diaria x dias). A regra de veiculo indisponivel continua valendo.")
    public ResponseEntity<?> alugar(@RequestBody AlugarRequest req) {
        try {
            Cliente cliente = clienteLogado();
            if (cliente == null) {
                return ResponseEntity.badRequest().body("Cadastro de cliente nao encontrado!");
            }
            if (req == null || req.veiculoId() == null || req.dataInicio() == null || req.dataFim() == null) {
                return ResponseEntity.badRequest().body("Informe o veiculo e as datas!");
            }
            if (req.dataInicio().isBefore(LocalDate.now())) {
                return ResponseEntity.badRequest().body("A data de inicio nao pode ser no passado!");
            }
            if (req.dataFim().isBefore(req.dataInicio())) {
                return ResponseEntity.badRequest().body("A data de fim nao pode ser anterior a data de inicio!");
            }
            Veiculo veiculo = veiculoRepository.findById(req.veiculoId()).orElse(null);
            if (veiculo == null) {
                return ResponseEntity.badRequest().body("Veiculo nao encontrado!");
            }

            long dias = Math.max(1, ChronoUnit.DAYS.between(req.dataInicio(), req.dataFim()));
            if (dias > 365) {
                return ResponseEntity.badRequest().body("O periodo maximo de locacao pelo site e de 365 dias!");
            }

            Locacao nova = new Locacao();
            nova.setDataInicio(req.dataInicio());
            nova.setDataFim(req.dataFim());
            nova.setValorTotal(veiculo.getValorDiaria() * dias);
            nova.setCliente(String.valueOf(cliente.getId()));
            nova.setVeiculo(String.valueOf(veiculo.getId()));
            return ResponseEntity.ok(locacaoService.criar(nova));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private Cliente clienteLogado() {
        String email = usuarioService.getUsuarioLogado().email();
        return clienteRepository.findFirstByEmailIgnoreCase(email).orElse(null);
    }
}
