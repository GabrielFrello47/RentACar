package br.com.senac.rentacar.domain.entities;

import br.com.senac.rentacar.application.DTOs.CriarAdminRequest;
import br.com.senac.rentacar.application.DTOs.UsuarioRequest;
import br.com.senac.rentacar.domain.valueobject.CPF;
import jakarta.persistence.*;
import lombok.*;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    private String nome;

    @Embedded
    private CPF cpf;

    // nunca deve aparecer em log (toString) nem ser devolvida pela API
    @ToString.Exclude
    private String senha;

    private String email;

    private String role = "ROLE_USER";

    private Long empresa_id;

    private EnumStatusUsuario status = EnumStatusUsuario.ATIVO;

    public void setEmail(String email) {
        if(email == null || email.isBlank() || !email.contains("@")){
            throw new IllegalArgumentException("Email Invalido!");
        }
        this.email = email.trim();
    }

    public Usuario(CriarAdminRequest criarAdminRequest) {
        this.setCpf(new CPF(criarAdminRequest.cpf()));
        this.setNome(criarAdminRequest.nome());
        this.setSenha(criarAdminRequest.senha());
        this.setEmail(criarAdminRequest.email());
        this.setRole("ROLE_ADMIN");

    }

    public Usuario(UsuarioRequest usuario) {
        this.setCpf(new CPF(usuario.cpf()));
        this.setNome(usuario.nome());
        this.setSenha(usuario.senha());
        this.setEmail(usuario.email());
        this.setRole("ROLE_USER");
    }



    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(this.role));
    }

    @Override
    public @Nullable String getPassword() {
        // a senha (hash) nunca e exposta pelo UserDetails
        return "";
    }

    @Override
    public String getUsername() {
        return this.email;
    }
}
