package Utilitaria;

public class ValidacionEntrada {

    /**
     * Esta clase sirve para manejar entradas de usuario relacionadas con cuentas bancarias.
     * Es utilitaria ya que encapsula la lógica de validación de cuentas.
     * Es una clase independiente de scannerutil porque usa ScannerUtil para capturar texto.
     * Pide al usuario un número de cuenta (cancelable) y verifica inmediatamente si existe.
     * Devuelve el número (trim) si existe, o null si el usuario cancela o la cuenta no existe.
     */
    public static String solicitarCuentaValida(Servicios.ATM atm, String prompt) {
        String entrada = ScannerUtil.capturarTextoCancelable(prompt);
        if (entrada == null) return null; // cancelado
        entrada = entrada.trim();
        if (!atm.existeCuenta(entrada)) {
            System.out.println("Cuenta no encontrada. Regresando al menú anterior.");
            return null;
        }
        return entrada;
    }

}
