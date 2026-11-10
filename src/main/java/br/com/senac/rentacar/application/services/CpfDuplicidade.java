package br.com.senac.rentacar.application.services;

import br.com.senac.rentacar.domain.respository.ClienteRepository;
import br.com.senac.rentacar.domain.respository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Confere se um CPF ja esta em uso. Compara so os digitos, entao "529.982.247-25" e "52998224725"
 * contam como o mesmo CPF (a base guarda o texto como foi digitado).
 */
@Component
public class CpfDuplicidade {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    private static String digitos(String cpf) {
        return cpf == null ? "" : cpf.replaceAll("\\D", "");
    }

    /** Ja existe outro usuario (qualquer role) com este CPF? */
    public boolean usuarioJaTem(String cpf, Long idUsuarioAtual) {
        String alvo = digitos(cpf);
        return usuarioRepository.findAll().stream()
                .filter(u -> u.getStatus() != br.com.senac.rentacar.domain.entities.EnumStatusUsuario.EXCLUIDO)
                .filter(u -> idUsuarioAtual == null || !idUsuarioAtual.equals(u.getId()))
                .anyMatch(u -> alvo.equals(digitos(String.valueOf(u.getCpf()))));
    }

    /** Ja existe outro cliente com este CPF? */
    public boolean clienteJaTem(String cpf, Long idClienteAtual) {
        String alvo = digitos(cpf);
        return clienteRepository.findAll().stream()
                .filter(c -> idClienteAtual == null || !idClienteAtual.equals(c.getId()))
                .anyMatch(c -> alvo.equals(digitos(c.getCpf())));
    }
}
