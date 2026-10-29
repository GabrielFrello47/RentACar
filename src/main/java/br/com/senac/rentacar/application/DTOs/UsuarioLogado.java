package br.com.senac.rentacar.application.DTOs;

import br.com.senac.rentacar.domain.entities.Usuario;
import br.com.senac.rentacar.domain.valueobject.CPF;

public record UsuarioLogado(Long id, String email, CPF cpf,
                            Long empresa_id,
                            java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> authorities) {

    public UsuarioLogado(Usuario usuario){
        this(
                usuario.getId(),
                usuario.getEmail(),
                usuario.getCpf(),
                1L,
                usuario.getAuthorities()
        );
    }
}
