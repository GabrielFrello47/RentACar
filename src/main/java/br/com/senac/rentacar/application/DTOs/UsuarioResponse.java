package br.com.senac.rentacar.application.DTOs;

import br.com.senac.rentacar.domain.entities.EnumStatusUsuario;
import br.com.senac.rentacar.domain.entities.Usuario;

public record UsuarioResponse(Long id, String nome, String cpf, String email, EnumStatusUsuario status, String role) {

    public UsuarioResponse(Usuario usuarioEntidade){

        this(
                usuarioEntidade.getId(),
                usuarioEntidade.getNome(),
                usuarioEntidade.getCpf() != null ? usuarioEntidade.getCpf().toString() : null,
                usuarioEntidade.getEmail(),
                usuarioEntidade.getStatus(),
                usuarioEntidade.getRole()
        );
    }
}
