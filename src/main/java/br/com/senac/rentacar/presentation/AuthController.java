package br.com.senac.rentacar.presentation;

import br.com.senac.rentacar.application.DTOs.LoginRequest;
import br.com.senac.rentacar.application.services.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticação controller",description = "Controller responsavel pela autencicação da aplicação!")
public class AuthController {

    @Autowired
    private UsuarioService usuarioService;

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Método responsavel por efetuar o login do usuário!")
    public ResponseEntity<?> login(@RequestBody LoginRequest resquest) {

        var resultadoAutenticacaoRetornoToken = usuarioService.validarUsuarioAutenticadoRetornaToken(resquest);

        if (resultadoAutenticacaoRetornoToken != null) {
            return ResponseEntity.ok(resultadoAutenticacaoRetornoToken);
        }
        return ResponseEntity.badRequest().body("Usuário ou senha Invalido!");

    }

    @PostMapping("/cadastro")
    @Operation(summary = "Cadastro de cliente", description = "Auto-cadastro publico do cliente (role ROLE_CLIENTE).")
    public ResponseEntity<?> cadastrar(@RequestBody br.com.senac.rentacar.application.DTOs.CadastroClienteRequest request) {
        try {
            return ResponseEntity.ok(usuarioService.cadastrarCliente(request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
