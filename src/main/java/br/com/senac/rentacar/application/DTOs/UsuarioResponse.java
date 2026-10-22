package br.com.senac.rentacar.application.DTOs;

import br.com.senac.rentacar.domain.entities.EnumStatusUsuario;
import br.com.senac.rentacar.domain.entities.Usuario;

public record UsuarioResponse(Long id, String nome, String cpf, String email, EnumStatusUsuario status) {

    public UsuarioResponse(Usuario usuarioEntidade){

        this(
                usuarioEntidade.getId(),
                usuarioEntidade.getNome(),
                usuarioEntidade.getCpf().toString(),
                usuarioEntidade.getEmail(),
                usuarioEntidade.getStatus()
        );
    }
}