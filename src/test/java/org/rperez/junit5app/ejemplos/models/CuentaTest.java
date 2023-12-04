package org.rperez.junit5app.ejemplos.models;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.rperez.junit5app.ejemplos.exceptions.DineroInsuficienteException;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class CuentaTest {

    @Test
    void testNombreCuenta() {
        Cuenta cuenta = new Cuenta("Andres", new BigDecimal("1000.1234"));
        String esperado = "Andres";
        String real = cuenta.getPersona();
        assertEquals(esperado, real);
        assertTrue(real.equals("Andres"));
    }

    @Test
    void testSaldoCuenta() {
        Cuenta cuenta = new Cuenta("Andres", new BigDecimal("1000.1234"));
        assertEquals(1000,1234, new BigDecimal("1000.1234").doubleValue());
        assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void testReferenciaCuenta() {
        Cuenta cuenta = new Cuenta("John Doe", new BigDecimal("8900.9997"));
        Cuenta cuenta2 = new Cuenta("John Doe", new BigDecimal("8900.9997"));
        assertEquals(cuenta2, cuenta);
    }

    @Test
    void testDebitoCuenta() {
        Cuenta cuenta = new Cuenta("rperez", new BigDecimal("1000.12345"));
        assertNotNull(cuenta.getSaldo());
        cuenta.debito(new BigDecimal("100"));
        assertEquals(900, cuenta.getSaldo().intValue());
        assertEquals("900.12345", cuenta.getSaldo().toPlainString());
    }

    @Test
    void testCreditoCuenta() {
        Cuenta cuenta = new Cuenta("rperez", new BigDecimal("1000.12345"));
        assertNotNull(cuenta.getSaldo());
        cuenta.credito(new BigDecimal("100"));
        assertEquals(1100, cuenta.getSaldo().intValue());
        assertEquals("1100.12345", cuenta.getSaldo().toPlainString());
    }

    @Test
    void testDineroInsuficiente() {
        Cuenta cuenta = new Cuenta("rperez", new BigDecimal("1000.12345"));
        Exception exception = assertThrows(DineroInsuficienteException.class, () -> {
            cuenta.debito(new BigDecimal("2000"));
        });
        String actual = exception.getMessage();
        String esperado = "Dinero insuficiente";
        assertEquals(esperado, actual);
    }

    @Test
    void testTransferirDineroEntreCuentas() {
        Cuenta cuenta1 = new Cuenta("rperez", new BigDecimal("2500"));
        Cuenta cuenta2 = new Cuenta("John Doe", new BigDecimal("1500.9997"));
        Banco banco = new Banco();
        banco.setNombre("Banco de España");
        banco.transferir(cuenta2, cuenta1, new BigDecimal(500));
        assertEquals("1000.9997", cuenta2.getSaldo().toPlainString());
        assertEquals("3000", cuenta1.getSaldo().toPlainString());
    }

    @Test
    void testRelacionBancoCuentas() {
        Cuenta cuenta1 = new Cuenta("rperez", new BigDecimal("2500"));
        Cuenta cuenta2 = new Cuenta("John Doe", new BigDecimal("1500.9997"));
        Banco banco = new Banco();
        banco.addCuenta(cuenta1);
        banco.addCuenta(cuenta2);
        banco.setNombre("Banco de España");
        banco.transferir(cuenta2, cuenta1, new BigDecimal(500));
        assertAll(
                () -> {assertEquals("1000.9997", cuenta2.getSaldo().toPlainString());},
                () -> {assertEquals("3000", cuenta1.getSaldo().toPlainString());},
                () -> {assertEquals(2, banco.getCuentas().size());},
                () -> {assertEquals("Banco de España", cuenta1.getBanco().getNombre());},
                () -> {assertEquals("rperez", banco.getCuentas().stream()
                        .filter(c -> c.getPersona().equals("rperez"))
                        .findFirst()
                        .get().getPersona());},
                () -> {assertTrue(banco.getCuentas().stream()
                        .anyMatch(c -> c.getPersona().equals("John Doe")));}
        );
    }

}