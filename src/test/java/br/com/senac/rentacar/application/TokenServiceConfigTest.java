package br.com.senac.rentacar.application;

import br.com.senac.rentacar.application.services.TokenService;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class TokenServiceConfigTest {

    @Test
    void naoSobeSemSegredoConfigurado() {
        var service = new TokenService();
        ReflectionTestUtils.setField(service, "secret", "");
        assertThrows(IllegalStateException.class, () -> ReflectionTestUtils.invokeMethod(service, "validarConfiguracao"));
    }

    @Test
    void naoSobeComSegredoCurto() {
        var service = new TokenService();
        ReflectionTestUtils.setField(service, "secret", "curto");
        assertThrows(IllegalStateException.class, () -> ReflectionTestUtils.invokeMethod(service, "validarConfiguracao"));
    }

    @Test
    void sobeComSegredoAdequado() {
        var service = new TokenService();
        ReflectionTestUtils.setField(service, "secret", "um-segredo-com-mais-de-16-caracteres");
        assertDoesNotThrow(() -> ReflectionTestUtils.invokeMethod(service, "validarConfiguracao"));
    }
}
