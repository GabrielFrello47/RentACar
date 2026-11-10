package br.com.senac.rentacar.domain;

import br.com.senac.rentacar.application.DTOs.CriarAdminRequest;
import br.com.senac.rentacar.application.DTOs.UsuarioRequest;
import br.com.senac.rentacar.domain.entities.EnumStatusUsuario;
import br.com.senac.rentacar.domain.entities.Usuario;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioTest {

    @Test
    void usuarioComumNasceAtivoComRoleUser() {
        var u = new Usuario(new UsuarioRequest("Ana", "segredo1", "529.982.247-25", "ana@teste.com"));
        assertEquals("ROLE_USER", u.getRole());
        assertEquals(EnumStatusUsuario.ATIVO, u.getStatus());
        assertEquals("ana@teste.com", u.getUsername());
    }

    @Test
    void adminRecebeRoleAdmin() {
        var u = new Usuario(new CriarAdminRequest("Adm", "adm@teste.com", "segredo1", "529.982.247-25", "x"));
        assertEquals("ROLE_ADMIN", u.getRole());
        assertEquals("ROLE_ADMIN", u.getAuthorities().iterator().next().getAuthority());
    }

    @Test
    void emailInvalidoOuNuloLancaIllegalArgument() {
        var u = new Usuario();
        assertThrows(IllegalArgumentException.class, () -> u.setEmail("semarroba"));
        assertThrows(IllegalArgumentException.class, () -> u.setEmail(null)); // antes dava NullPointerException
        assertThrows(IllegalArgumentException.class, () -> u.setEmail("  "));
    }

    @Test
    void senhaNaoApareceNoToString() {
        var u = new Usuario(new UsuarioRequest("Ana", "senha-super-secreta", "529.982.247-25", "ana@teste.com"));
        assertFalse(u.toString().contains("senha-super-secreta"));
    }
}
