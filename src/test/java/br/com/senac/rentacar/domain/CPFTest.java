package br.com.senac.rentacar.domain;

import br.com.senac.rentacar.domain.valueobject.CPF;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CPFTest {

    @Test
    void aceitaCpfValidoComESemMascara() {
        assertTrue(CPF.isValidCpf("529.982.247-25"));
        assertTrue(CPF.isValidCpf("52998224725"));
        assertTrue(CPF.isValidCpf("111.444.777-35"));
    }

    @Test
    void rejeitaCpfInvalido() {
        assertFalse(CPF.isValidCpf(null));
        assertFalse(CPF.isValidCpf(""));
        assertFalse(CPF.isValidCpf("123"));
        assertFalse(CPF.isValidCpf("111.111.111-11"));     // todos os digitos iguais
        assertFalse(CPF.isValidCpf("529.982.247-24"));     // digito verificador errado
        assertFalse(CPF.isValidCpf("abcdefghijk"));
    }

    @Test
    void construtorLancaExcecaoParaCpfInvalido() {
        assertThrows(IllegalArgumentException.class, () -> new CPF("000.000.000-00"));
    }

    @Test
    void toStringFormataOCpf() {
        assertEquals("529.982.247-25", new CPF("52998224725").toString());
        assertEquals("529.982.247-25", new CPF("529.982.247-25").toString());
    }
}
