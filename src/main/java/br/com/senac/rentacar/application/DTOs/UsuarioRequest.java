package br.com.senac.rentacar.application.DTOs;

public record UsuarioRequest(String nome, String senha, String cpf, String email) {
}
