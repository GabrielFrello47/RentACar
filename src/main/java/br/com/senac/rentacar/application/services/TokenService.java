package br.com.senac.rentacar.application.services;

import br.com.senac.rentacar.application.DTOs.UsuarioLogado;
import br.com.senac.rentacar.domain.entities.EnumStatusUsuario;
import br.com.senac.rentacar.domain.entities.Token;
import br.com.senac.rentacar.domain.entities.Usuario;
import br.com.senac.rentacar.domain.respository.TokenRepository;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class TokenService {

    private static final String EMISSOR = "RentACar";
    private static final int TAMANHO_MINIMO_SEGREDO = 16;

    @Value("${spring.secret}")
    private String secret;

    @Value("${spring.tempo_expiracao}")
    private Long tempo;

    @Autowired
    private TokenRepository tokenRepository;

    /**
     * Falha logo na subida se o segredo nao foi configurado,
     * em vez de assinar tokens com uma chave vazia/fraca.
     */
    @PostConstruct
    void validarConfiguracao() {
        if (secret == null || secret.isBlank() || secret.length() < TAMANHO_MINIMO_SEGREDO) {
            throw new IllegalStateException(
                    "Defina spring.secret (variavel RENTACAR_SECRET ou local.properties) com pelo menos "
                            + TAMANHO_MINIMO_SEGREDO + " caracteres.");
        }
    }

    @Transactional
    public String gerarToken(Usuario usuario) {

        Algorithm algorithm = Algorithm.HMAC256(secret);

        // tokens vencidos nao servem mais: evita a tabela crescer para sempre
        tokenRepository.deleteByDataCadastroBefore(LocalDateTime.now().minusMinutes(tempo));

        String token = JWT.create()
                .withIssuer(EMISSOR)
                .withSubject(usuario.getEmail())
                // id unico: dois logins no mesmo segundo nao podem gerar o mesmo token (linhas repetidas na tabela token)
                .withJWTId(UUID.randomUUID().toString())
                .withExpiresAt(this.gerarDataExpiracao())
                .sign(algorithm);

        tokenRepository.save(new Token(token, usuario));

        return token;
    }

    public UsuarioLogado verificarToken(String token) throws JWTVerificationException {
        Algorithm algorithm = Algorithm.HMAC256(secret);

        JWTVerifier verificador = JWT.require(algorithm).withIssuer(EMISSOR).build();

        verificador.verify(token);

        var tokenBanco = tokenRepository.findTokenByToken(token).orElse(null);

        if (tokenBanco == null || tokenBanco.getUsuario() == null) {
            throw new IllegalArgumentException("Token Invalido!");
        }

        // usuario bloqueado/excluido perde o acesso na hora, mesmo com token ainda valido
        if (tokenBanco.getUsuario().getStatus() != EnumStatusUsuario.ATIVO) {
            throw new IllegalArgumentException("Usuario sem acesso!");
        }

        return new UsuarioLogado(tokenBanco.getUsuario());
    }

    private Instant gerarDataExpiracao() {
        // Instant nao depende de fuso horario (antes dependia do fuso do servidor)
        return Instant.now().plus(tempo, ChronoUnit.MINUTES);
    }

}
