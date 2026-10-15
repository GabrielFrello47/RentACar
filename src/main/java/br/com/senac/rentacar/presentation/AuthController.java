package br.com.senac.rentacar.presentation;

import br.com.senac.rentacar.application.DTOs.EsqueciSenhaRequest;
import br.com.senac.rentacar.application.DTOs.LoginRequest;
import br.com.senac.rentacar.application.DTOs.LoginResponse;
import br.com.senac.rentacar.application.DTOs.RedefinirSenhaRequest;
import br.com.senac.rentacar.application.services.UsuarioService;
import br.com.senac.rentacar.domain.respository.UsuarioRepository;
import br.com.senac.rentacar.application.services.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.HttpURLConnection;


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
    }
   // @PostMapping("/esqueci-senha")
  //  @Operation(description = "Gera um token de recuperação de senha", summary = "Esqueci minha senha")
    //public ResponseEntity<?> esqueciSenha(@RequestBody EsqueciSenhaRequest esqueciSenhaRequest) {
//var token = tokenService.gerarTokenRecuperacaoSenha(esqueciSenhaRequest.email());

      //  System.out.println("Token de recuperação para " + esqueciSenhaRequest.email() + ": " + token);

        //return ResponseEntity.ok("Um link de recuperação foi enviado para o seu e-mail.");


    //@PostMapping("/redefinir-senha")
    //@Operation(description = "Redefine a senha do usuario a partir do token de recuperação", summary = "Redefinir senha")
    //public ResponseEntity<?> redefinirSenha(@RequestBody RedefinirSenhaRequest redefinirSenhaRequest) {

      //  try {
        //    var jwtValidador = tokenService.verificarToken(redefinirSenhaRequest.token());

 //           var tipo = jwtValidador.getClaim("tipo").asString();

   //         if (!"recuperacao-senha".equals(tipo)) {
     //           return ResponseEntity.status(HttpURLConnection.HTTP_BAD_REQUEST).build();
       //     }

         //   var email = jwtValidador.getSubject();

            // Em um cenario real, atualizariamos a senha no banco de dados
           // System.out.println("Senha do usuario " + email + " redefinida para: " + redefinirSenhaRequest.novaSenha());

           // return ResponseEntity.ok("Senha redefinida com sucesso.");

       // } catch (Exception e) {
         //   return ResponseEntity.status(HttpURLConnection.HTTP_UNAUTHORIZED).body("Token invalido ou expirado");
        //}
    //}

//}