package br.com.senac.rentacar.presentation;

import br.com.senac.rentacar.application.DTOs.*;
import br.com.senac.rentacar.application.services.UsuarioService;
import br.com.senac.rentacar.domain.entities.EnumStatusUsuario;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/usuarios")
public class UsuarioController {


    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> listarTodos(){

        return  ResponseEntity.ok(usuarioService.listarTodosUsuariosTable());
    }

    @PostMapping("/admin")
    @Operation(summary = "Criar administrador", description = "Cria um administrador. Exige a secretKey do servidor (usada pelo aplicativo desktop).")
    public ResponseEntity<?> criarAdmin(@RequestBody CriarAdminRequest criarAdminRequest){

        try{
            CriarAdminResponse respostaSalvar = usuarioService.criarAdmin(criarAdminRequest);

            if (respostaSalvar == null) {
                // antes devolvia 200 "salvo com sucesso" mesmo com a chave errada
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Chave invalida!");
            }
            return ResponseEntity.ok(respostaSalvar);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }

    @GetMapping("/buscarusuariologado")
    @Operation(summary = "Usuario logado", description = "Devolve os dados do usuario dono do token enviado.")
    public ResponseEntity<UsuarioResponse> buscarUsuarioLogado(){

        UsuarioResponse usuarioLogado = usuarioService.buscarUsuarioLogado();
        if(usuarioLogado != null){
            return ResponseEntity.ok(usuarioLogado);
        }

        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> buscarPorId(@PathVariable Long id){

        UsuarioResponse usuarioBanco = usuarioService.buscarPorId(id);
        if(usuarioBanco!= null ){
            return  ResponseEntity.ok(usuarioBanco);
        }

        return  ResponseEntity.notFound().build();
    }



    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> criar(@RequestBody UsuarioRequest usuario){

        try {
            UsuarioResponse usuarioBanco = usuarioService.salvarUsuario(usuario,0L);
            return ResponseEntity.ok(usuarioBanco);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> atualizarStatus(@PathVariable Long id, @RequestBody AtualizarStatusRequest statusRequest){

        if (statusRequest == null || statusRequest.status() == null) {
            // antes um corpo sem "status" gravava null e o usuario ficava sem conseguir logar
            return ResponseEntity.badRequest().build();
        }

        var atualizarUsuario = usuarioService.atualizarStatus(statusRequest.status(),id);

        if(atualizarUsuario){
            return ResponseEntity.ok().build();
        }

        return  ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable Long id, @RequestBody UsuarioRequest usuario){

        try {
            UsuarioResponse usuarioBanco = usuarioService.salvarUsuario(usuario,id);
            if (usuarioBanco == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(usuarioBanco);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }

    @DeleteMapping("/{id}/excluir")
    public ResponseEntity<Void> excluir(@PathVariable Long id){

        var atualizarUsuario = usuarioService.atualizarStatus(EnumStatusUsuario.EXCLUIDO,id);

        if(atualizarUsuario){
            return ResponseEntity.ok().build();
        }

        return  ResponseEntity.notFound().build();

    }


}
