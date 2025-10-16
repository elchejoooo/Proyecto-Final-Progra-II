package Servicios;

import Modelos.Cuenta;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Clase de reportes: guarda cuentas en ControlCuentas.txt y permite consultar saldos bajos.
 */
public class Reportes {
    public static final String CONTROL_FILE = "ControlCuentas.txt";

    /** Guarda todas las cuentas en el archivo ControlCuentas.txt con formato: numero|titular|tipo|saldo */
    public static void guardarTodasCuentas(List<Cuenta> cuentas) {
        File f = new File(CONTROL_FILE);
        // Si no hay cuentas en memoria, no sobrescribimos; preservamos el archivo existente
        if (cuentas == null || cuentas.isEmpty()) {
            return;
        }

        // Leer contenido existente y mapear por numero de cuenta
        java.util.Map<String, String> existentes = new java.util.LinkedHashMap<>();
        if (f.exists()) {
            try (BufferedReader r = new BufferedReader(new FileReader(f))) {
                String linea;
                while ((linea = r.readLine()) != null) {
                    String[] parts = linea.split("\\|");
                    if (parts.length >= 1) {
                        String num = parts[0];
                        existentes.put(num, linea);
                    }
                }
            } catch (IOException e) {
                System.out.println("Advertencia: no se pudo leer ControlCuentas.txt antes de escribir: " + e.getMessage());
            }
        }

        // Actualizar/insertar las cuentas en el mapa
        for (Cuenta c : cuentas) {
            String linea = c.getNumeroCuenta() + "|" + c.getTitular().getNombreCompleto() + "|" + c.getTitular().getIdCliente() + "|" + c.getTipoCuenta() + "|" + String.format("%.2f", c.getSaldo());
            existentes.put(c.getNumeroCuenta(), linea);
        }

        // Escribir el archivo con el contenido combinado (preserva orden de existentes donde no se actualizaron)
        try (BufferedWriter w = new BufferedWriter(new FileWriter(f, false))) {
            for (String linea : existentes.values()) {
                w.write(linea);
                w.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error escribiendo ControlCuentas.txt: " + e.getMessage());
        }
    }

    /** Lee ControlCuentas.txt y muestra cuentas con saldo menor al umbral */
    public static void imprimirCuentasConSaldoMenor(double umbral) {
        File f = new File(CONTROL_FILE);
        if (!f.exists()) {
            System.out.println("Archivo de control no existe: " + CONTROL_FILE);
            return;
        }

        try (BufferedReader r = new BufferedReader(new FileReader(f))) {
            String linea;
            boolean any = false;
            while ((linea = r.readLine()) != null) {
                String[] parts = linea.split("\\|");
                // aceptar formatos antiguos (4 partes) o nuevo (5 partes con idCliente)
                if (parts.length < 4) continue;
                String numero = parts[0];
                String titular = parts[1];
                String idCliente = "";
                String tipo = "";
                String saldoStr = "0";
                if (parts.length == 4) {
                    // antiguo: numero|titular|tipo|saldo
                    tipo = parts[2];
                    saldoStr = parts[3];
                } else {
                    // nuevo: numero|titular|idCliente|tipo|saldo
                    idCliente = parts[2];
                    tipo = parts[3];
                    saldoStr = parts[4];
                }
                // normalizar coma decimal a punto
                saldoStr = saldoStr.replace(',', '.').trim();
                double saldo = 0.0;
                try { saldo = Double.parseDouble(saldoStr); } catch (NumberFormatException ex) { continue; }
                if (saldo < umbral) {
                    any = true;
                    String extra = idCliente.isEmpty() ? "" : (" | ID: " + idCliente);
                    System.out.println("Cuenta: " + numero + " | Titular: " + titular + extra + " | Tipo: " + tipo + " | Saldo: " + String.format("%.2f", saldo));
                }
            }
            if (!any) System.out.println("No se encontraron cuentas con saldo menor a " + String.format("%.2f", umbral));
        } catch (IOException e) {
            System.out.println("Error leyendo ControlCuentas.txt: " + e.getMessage());
        }
    }

    /** Menu simple para reportes (actualmente solo consulta saldos bajos) */
    public void menuReportes() {
        System.out.println("\nMenu de Reportes:\n1) Consulta Cuentas con Saldos Bajos\n2) Volver");
        String opt = Utilitaria.ScannerUtil.capturarTexto("Elija una opción de reportes:");
        if (opt == null) return;
        opt = opt.trim();
        switch (opt) {
            case "1":
                imprimirCuentasConSaldoMenor(200.00);
                break;
            default:
                // volver
                break;
        }
    }
}

