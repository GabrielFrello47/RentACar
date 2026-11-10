package br.com.senac.rentacar.application.DTOs;

/** Auto-cadastro do cliente pela landing page. */
public record CadastroClienteRequest(String nome, String cpf, String telefone, String email, String senha) {
}
