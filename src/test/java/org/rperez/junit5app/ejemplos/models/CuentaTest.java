package org.rperez.junit5app.ejemplos.models;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.rperez.junit5app.ejemplos.exceptions.DineroInsuficienteException;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

class CuentaTest {

    Cuenta cuenta;

    @BeforeAll
    static void beforeAll() {
        System.out.println("Inicializando la clase CuentaTest");
    }

    @AfterAll
    static void afterAll() {
        System.out.println("Finalizando la clase CuentaTest");
    }

    @BeforeEach
    void initMetodoTest(TestInfo testInfo, TestReporter testReporter) {
        this.cuenta = new Cuenta("Andres", new BigDecimal("1000.1234"));
        System.out.println("Iniciando el método");
        // En lugar de por la salida estándar lo sacamos por la propia consola de JUnit
        testReporter.publishEntry("Ejecutando: " + testInfo.getDisplayName() + " " + testInfo.getTestMethod().get().getName());
    }

    @AfterEach
    void endMetodoTest() {
        System.out.println("Finalizando el método");
    }

    // Esto es una inner class. La vamos a utilizar para organizar nuestros tests
    // Para que la tenga en cuenta Junit le ponemos la anotación Nested
    @Nested
    @DisplayName("Clase con los tests para el nombre y saldo de la cuenta")
    class testCuentaNombreSaldo {
        @Test
        @DisplayName("Probando el nombre de la cuenta")
        void testNombreCuenta() {
            String esperado = "Andres";
            String real = cuenta.getPersona();
            assertNotNull(real, () -> "La cuenta no puede ser nula");
            assertEquals(esperado, real, () -> "El nombre de la cuenta no es el esperado");
            assertTrue(real.equals("Andres"), () -> "El nombre real no coincide con el esperado");
        }

        @Test
        @DisplayName("Probando el saldo de la cuenta")
        void testSaldoCuenta() {
            assertEquals(1000,1234, new BigDecimal("1000.1234").doubleValue());
            assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
        }

        @Test
        @DisplayName("Probando que las referencias sean iguales con el método equals")
        void testReferenciaCuenta() {
            Cuenta cuenta = new Cuenta("John Doe", new BigDecimal("8900.9997"));
            Cuenta cuenta2 = new Cuenta("John Doe", new BigDecimal("8900.9997"));
            assertEquals(cuenta2, cuenta);
        }
    }

    @Nested
    class testCuentaOperaciones {
        @Test
        void testDebitoCuenta() {
            assertNotNull(cuenta.getSaldo());
            cuenta.debito(new BigDecimal("100"));
            assertEquals(900, cuenta.getSaldo().intValue());
            assertEquals("900.1234", cuenta.getSaldo().toPlainString());
        }

        @Test
        void testCreditoCuenta() {
            assertNotNull(cuenta.getSaldo());
            cuenta.credito(new BigDecimal("100"));
            assertEquals(1100, cuenta.getSaldo().intValue());
            assertEquals("1100.1234", cuenta.getSaldo().toPlainString());
        }

        @Test
        void testDineroInsuficiente() {
            Exception exception = assertThrows(DineroInsuficienteException.class, () -> {
                cuenta.debito(new BigDecimal("2000"));
            });
            String actual = exception.getMessage();
            String esperado = "Dinero insuficiente";
            assertEquals(esperado, actual);
        }

        @Test
        @Disabled
        void testTransferirDineroEntreCuentas() {
            fail();
            Cuenta cuenta1 = new Cuenta("rperez", new BigDecimal("2500"));
            Cuenta cuenta2 = new Cuenta("John Doe", new BigDecimal("1500.9997"));
            Banco banco = new Banco();
            banco.setNombre("Banco de España");
            banco.transferir(cuenta2, cuenta1, new BigDecimal(500));
            assertEquals("1000.9997", cuenta2.getSaldo().toPlainString());
            assertEquals("3000", cuenta1.getSaldo().toPlainString());
        }
    }

    @Test
    @DisplayName("Probando relaciones entre el banco y las cuentas con assertAll")
    void testRelacionBancoCuentas() {
        Cuenta cuenta1 = new Cuenta("rperez", new BigDecimal("2500"));
        Cuenta cuenta2 = new Cuenta("John Doe", new BigDecimal("1500.9997"));
        Banco banco = new Banco();
        banco.addCuenta(cuenta1);
        banco.addCuenta(cuenta2);
        banco.setNombre("Banco de España");
        banco.transferir(cuenta2, cuenta1, new BigDecimal(500));
        assertAll(
                () -> {assertEquals("1000.9997", cuenta2.getSaldo().toPlainString(), () -> "El valor de la cuenta2 no es el esperado");},
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

    @Nested
    class testByOS {
        @Test
        @EnabledOnOs(OS.WINDOWS)
        void testSoloWindows() {
        }

        @Test
        @EnabledOnOs({OS.LINUX, OS.MAC})
        void testLinuxMac() {
        }

        @Test
        @DisabledOnOs(OS.WINDOWS)
        void testNoWindows() {
        }
    }

    @Nested
    class testByJavaVersion{
        @Test
        @EnabledOnJre(JRE.JAVA_8)
        void testSoloJava8() {
        }

        @Test
        @DisabledOnJre(JRE.JAVA_15)
        void testNoJava15() {
        }
    }

    @Nested
    class testBySystemProperties {
        @Test
        void testImprimirSystemProperties() {
            Properties properties = System.getProperties();
            properties.forEach((k,v) -> System.out.println(k + ": " + v));
        }

        @Test
        @EnabledIfSystemProperty(named = "java.version", matches = "17.0.4")
        void testOnlyIfJavaVersionMatches() {
        }

        @Test
        @DisabledIfSystemProperty(named = "os.arch", matches = ".*32.*")
        void testOnlyIf64() {
        }

        @Test
        @EnabledIfSystemProperty(named = "user.name", matches = "jimenahernando")
        void testOnlyIfUserNameMatches() {
        }

        @Test
        @EnabledIfSystemProperty(named = "ENV", matches = "dev")
        void testOnlyDev() {
            // Variable personalizada
            // Hemos añadido -DENV=dev a CuentaTest en las Run/Debug Configurations para que tenga en cuenta esa variable de entorno
            // Para que lo tenga en cuenta en la ejecución debemos hacer run de esa configuración expecíficamente (la general no lo tendrá en cuenta)
        }
    }

    @Nested
    class testByEnvVar {
        @Test
        void testImprimirVariablesAmbiente() {
            Map<String, String> getenv = System.getenv();
            getenv.forEach((k,v) -> System.out.println(k + ": " + v));
        }

        @Test
        @EnabledIfEnvironmentVariable(named = "LOGNAME", matches = "jimenahernando")
        void testLogName() {
        }

        @Test
        @EnabledIfEnvironmentVariable(named = "ENVIROMENT", matches = "dev")
        void testBySpecificEnvironment() {
            // Variable personalizada
            // Hemos añadido ENVIROMENT=dev a CuentaTest en las Run/Debug Configurations en la parte de Enviroment Variables
            // Para que lo tenga en cuenta en la ejecución debemos hacer run de esa configuración expecíficamente (la general no lo tendrá en cuenta)
        }
    }

    @Test
    @DisplayName("Probando el saldo de la cuenta (sólo si el entorno es desarrollo)")
    void testSaldoCuentaDev() {
        boolean esDev = "dev".equals(System.getProperty("ENV"));
        assumeTrue(esDev);
        // Todo lo que queda debajo del assumeTrue sólo se ejecuta si cumple la condición
        assertEquals(1000,1234, new BigDecimal("1000.1234").doubleValue());
        assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    @DisplayName("Probando el saldo de la cuenta (si el entorno es desarrollo ejecuto algo)")
    void testSaldoCuentaDev2() {
        boolean esDev = "dev".equals(System.getProperty("ENV"));
        assumingThat(esDev, () -> {
            assertEquals(1000,1234, new BigDecimal("1000.1234").doubleValue());
        });
        // Este se ejecutará siempre a diferencia de en el método anterior
        assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
    }

    @RepeatedTest(5)
    void testDebitoCuentaRepeated(RepetitionInfo info) {
        if (info.getCurrentRepetition() == 3) {
            System.out.println("Repetición número 3");
        }
        assertNotNull(cuenta.getSaldo());
        cuenta.debito(new BigDecimal("100"));
        assertEquals(900, cuenta.getSaldo().intValue());
        assertEquals("900.1234", cuenta.getSaldo().toPlainString());
    }

    // Mediante tags podemos agrupar un conjunto de tests que queramos ejecutar (los que tengan dicho tag)
    // En las configuraciones podemos seleccionar Tag en lugar de Class para indicar que sólo queremos ejecutar esas y no todas
    @Tag("parametrizada")
    @Nested
    class testParametrizados{

        @ParameterizedTest(name="Número de prueba {index} ejecutando con valor {0}")
        @ValueSource(ints = {100, 200, 500, 1000})
        void testDebitoCuentaParametrizado(int monto) {
            cuenta.debito(new BigDecimal(monto));
            assertNotNull(cuenta.getSaldo());
            assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
        }

        @ParameterizedTest
        @CsvSource({"100", "250", "600"})
        void testDebitoCuentaParametrizadoCSV(String monto) {
            cuenta.debito(new BigDecimal(monto));
            assertNotNull(cuenta.getSaldo());
            assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
        }

        @ParameterizedTest
        // Saldo, Monto
        @CsvSource({"200,100", "280,250", "3000,600"})
        void testDebitoCuentaParametrizadoCSV2(String saldo, String monto) {
            cuenta.setSaldo(new BigDecimal(saldo));
            cuenta.debito(new BigDecimal(monto));
            assertNotNull(cuenta.getSaldo());
            assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
        }

        @ParameterizedTest
        // Es importante que tiene que estar dentro de la carpeta de resources
        @CsvFileSource(resources = "/data.csv")
        void testDebitoCuentaParametrizadoCSVFile(String monto) {
            cuenta.debito(new BigDecimal(monto));
            assertNotNull(cuenta.getSaldo());
            assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
        }

        @ParameterizedTest
        @CsvFileSource(resources = "/data2.csv")
        void testDebitoCuentaParametrizadoCSVFile2(String saldo, String monto) {
            cuenta.setSaldo(new BigDecimal(saldo));
            cuenta.debito(new BigDecimal(monto));
            assertNotNull(cuenta.getSaldo());
            assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
        }
    }

    @Tag("parametrizada")
    @ParameterizedTest
    @MethodSource("montoList")
    void testDebitoCuentaParametrizadoByMethod(String monto) {
        cuenta.debito(new BigDecimal(monto));
        assertNotNull(cuenta.getSaldo());
        assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
    }

    static List<String> montoList() {
        return Arrays.asList("100", "400", "850");
    }

    @Nested
    @Tag("timeout")
    class testWithTimeout {
        @Test
        @Timeout(2) // En segundos
        void testTimeOut() throws InterruptedException {
            TimeUnit.SECONDS.sleep(1);
        }

        @Test
        @Timeout(value = 2000, unit = TimeUnit.MILLISECONDS)
        void testTimeOutV2() throws InterruptedException {
            TimeUnit.SECONDS.sleep(1);
        }

        @Test
        void testTimeOutV3() throws InterruptedException {
            assertTimeout(Duration.ofMillis(500), () -> {
                TimeUnit.MILLISECONDS.sleep(100);
            });
        }
    }
}