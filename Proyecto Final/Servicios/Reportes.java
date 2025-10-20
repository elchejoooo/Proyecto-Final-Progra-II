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
    public static final String CONTROL_CLIENTES_FILE = "ControlClientes.txt";
    public static final String CONTROL_TRANS_FILE = "HistorialTransacciones.txt";

    /** Guarda todas las cuentas en el archivo ControlCuentas.txt con formato: numero|titular|tipo|saldo */
    public static void guardarTodasCuentas(List<Cuenta> cuentas) {
        File f = new File(CONTROL_FILE);
        // Sobrescribir el archivo exactamente con la lista actual (refleja eliminaciones)
        try (BufferedWriter w = new BufferedWriter(new FileWriter(f, false))) {
            for (Cuenta c : cuentas) {
                // Nuevo formato: numero|titular|idCliente|tipo|pin|saldo
                String linea = c.getNumeroCuenta() + "|" + c.getTitular().getNombreCompleto() + "|" + c.getTitular().getIdCliente() + "|" + c.getTipoCuenta() + "|" + c.getPin() + "|" + String.format("%.2f", c.getSaldo());
                w.write(linea);
                w.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error escribiendo ControlCuentas.txt: " + e.getMessage());
        }
    }

    /** Guarda todos los clientes en ControlClientes.txt con formato: idCliente|nombreCompleto|telefono|fechaNacimiento */
    public static void guardarTodosClientes(List<Modelos.Cliente> clientes) {
        File f = new File(CONTROL_CLIENTES_FILE);
        // Sobrescribir el archivo exactamente con la lista actual (refleja eliminaciones)
        try (BufferedWriter w = new BufferedWriter(new FileWriter(f, false))) {
            if (clientes != null) {// verificar que la lista no sea nula
                for (Modelos.Cliente c : clientes) {// recorrer cada cliente
                    String linea = c.getIdCliente() + "|" + c.getNombreCompleto() + "|" + c.getTelefono() + "|" + c.getFechaNacimiento();// crear la linea con los datos del cliente
                    w.write(linea); w.newLine();
                }
            }
        } catch (IOException e) { System.out.println("Error escribiendo ControlClientes.txt: " + e.getMessage()); }// manejar excepcion
    }

    /** Lee ControlClientes.txt y devuelve un map id->linea (no instancia objetos Cliente aquí) */
    public static java.util.Map<String, String> leerTodasLineasClientes() {
        java.util.Map<String, String> existentes = new java.util.LinkedHashMap<>();// mantener orden de insercion
        File f = new File(CONTROL_CLIENTES_FILE);//verifica que el archivo exista
        if (!f.exists()) return existentes;//si no existe, devuelve mapa vacio
        try (BufferedReader r = new BufferedReader(new FileReader(f))) {//lee el archivo
            String linea;
            while ((linea = r.readLine()) != null) {//lee cada linea
                String[] parts = linea.split("\\|");//separa por |
                if (parts.length >= 1) existentes.put(parts[0], linea);//usa el primer campo como clave (idCliente) y la linea completa como valor
            }
        } catch (IOException e) { System.out.println("Error leyendo ControlClientes.txt: " + e.getMessage()); }
        return existentes;
    }

    /** Lee ControlCuentas.txt y devuelve un map numeroCuenta->linea (no instancia objetos Cuenta aquí) */
    public static java.util.Map<String, String> leerTodasLineasCuentas() {
        java.util.Map<String, String> existentes = new java.util.LinkedHashMap<>();// mantener orden de insercion
        File f = new File(CONTROL_FILE);//verifica que el archivo exista
        if (!f.exists()) return existentes;
        try (BufferedReader r = new BufferedReader(new FileReader(f))) {//lee el archivo
            String linea;
            while ((linea = r.readLine()) != null) {//lee cada linea
                String[] parts = linea.split("\\|");//separa por |
                if (parts.length >= 1) existentes.put(parts[0], linea);//usa el primer campo como clave (numeroCuenta) y la linea completa como valor
            }
        } catch (IOException e) { System.out.println("Error leyendo ControlCuentas.txt: " + e.getMessage()); }// manejar excepcion
        return existentes;
    }

    /** Guarda una lista completa de transacciones en HistorialTransacciones.txt con formato:
     * idTransaccion|numeroCuenta|tipo|monto|fechaHora(ISO)
     */
    public static void guardarTodasTransacciones(List<Modelos.Transaccion> transacciones) {
        File f = new File(CONTROL_TRANS_FILE);
        try (BufferedWriter w = new BufferedWriter(new FileWriter(f, false))) {
            for (Modelos.Transaccion t : transacciones) {
                String linea = t.getIdTransaccion() + "|" + t.getNumeroCuenta() + "|" + t.getTipoTransaccion() + "|" + String.format("%.2f", t.getMonto()) + "|" + t.getFechaHora().toString();
                w.write(linea); w.newLine();
            }
        } catch (IOException e) { System.out.println("Error escribiendo HistorialTransacciones.txt: " + e.getMessage()); }
    }

    /** Agrega (append) una sola transacción al historial (útil para operaciones en vivo) */
    public static void appendTransaccion(Modelos.Transaccion t) {
        File f = new File(CONTROL_TRANS_FILE);
        try (BufferedWriter w = new BufferedWriter(new FileWriter(f, true))) {
            String linea = t.getIdTransaccion() + "|" + t.getNumeroCuenta() + "|" + t.getTipoTransaccion() + "|" + String.format("%.2f", t.getMonto()) + "|" + t.getFechaHora().toString();
            w.write(linea); w.newLine();
        } catch (IOException e) { System.out.println("Error anexando a HistorialTransacciones.txt: " + e.getMessage()); }
    }

    /** Lee todas las transacciones del archivo y devuelve la lista de líneas (no instancia objetos aquí) */
    public static java.util.List<String> leerTodasLineasTransacciones() {
        java.util.List<String> out = new java.util.ArrayList<>();
        File f = new File(CONTROL_TRANS_FILE);
        if (!f.exists()) return out;
        try (BufferedReader r = new BufferedReader(new FileReader(f))) {
            String linea;
            while ((linea = r.readLine()) != null) out.add(linea);
        } catch (IOException e) { System.out.println("Error leyendo HistorialTransacciones.txt: " + e.getMessage()); }
        return out;
    }

    /** Lee ControlCuentas.txt y muestra cuentas con saldo menor al umbral */
    public static void imprimirCuentasConSaldoMenor(double umbral) {
        File f = new File(CONTROL_FILE);//verifica que el archivo exista
        if (!f.exists()) {
            System.out.println("Archivo de control no existe: " + CONTROL_FILE);
            return;
        }

        try (BufferedReader r = new BufferedReader(new FileReader(f))) {//lee el archivo, maneja excepcion, cierra recurso
            String linea;
            boolean any = false;
            while ((linea = r.readLine()) != null) {
                String[] parts = linea.split("\\|");//en esta parte separamos la linea por |, el \\ es para escapar el caracter especial
                // aceptar formatos antiguos (4 partes), previos (5 partes) o nuevo (6 partes con pin)
                if (parts.length < 4) continue;
                String numero = parts[0];
                String titular = parts[1];
                String idCliente = "";
                String tipo = "";
                String pin = "";
                String saldoStr = "0";
                if (parts.length == 4) {
                    // antiguo: numero|titular|tipo|saldo
                    tipo = parts[2];
                    saldoStr = parts[3];
                } else if (parts.length == 5) {
                    // intermedio: numero|titular|idCliente|tipo|saldo
                    idCliente = parts[2];
                    tipo = parts[3];
                    saldoStr = parts[4];
                } else {
                    // nuevo: numero|titular|idCliente|tipo|pin|saldo
                    idCliente = parts[2];
                    tipo = parts[3];
                    pin = parts[4];
                    saldoStr = parts[5];
                }
                // normalizar coma decimal a punto
                saldoStr = saldoStr.replace(',', '.').trim();// en caso de que usen coma como separador decimal
                double saldo = 0.0;
                try { saldo = Double.parseDouble(saldoStr); } catch (NumberFormatException ex) { continue; }// si no es un numero valido, saltar
                if (saldo < umbral) {
                    any = true;// marcar que se encontro al menos una cuenta
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
        System.out.println("\nMenu de Reportes:\n1) Consulta Cuentas con Saldos Bajos\n2) Movimientos de cuenta especifica\n3) Volver");
        String opt = Utilitaria.ScannerUtil.capturarTexto("Elija una opción de reportes:");
        if (opt == null) return;
        opt = opt.trim();
        switch (opt) {
            case "1":
                imprimirCuentasConSaldoMenor(200.00);
                break;
            case "2":
                mostrarMovimientosCuenta();
                break;
            default:
                // volver
                break;
        }
    }

    /** Muestra movimientos de una cuenta específica pidiendo número y PIN */
    public void mostrarMovimientosCuenta() {
        String numero = Utilitaria.ScannerUtil.capturarTexto("Ingrese número de cuenta:");
        if (numero == null) return;
        numero = numero.trim();

        // buscar en control de cuentas para obtener PIN e idCliente
        java.util.Map<String,String> cuentas = leerTodasLineasCuentas();
        String linea = cuentas.get(numero);
        if (linea == null) {
            System.out.println("Cuenta no encontrada en ControlCuentas.txt: " + numero);
            return;
        }
        String[] parts = linea.split("\\|");
        String pin = "";
        if (parts.length >= 6) pin = parts[4];

        String pinIn = Utilitaria.ScannerUtil.capturarTexto("Ingrese PIN para la cuenta " + numero + ":");
        if (pinIn == null) return;
        if (!pinIn.equals(pin)) { System.out.println("PIN inválido."); return; }

        // leer transacciones y filtrar por cuenta
        java.util.List<String> txLines = leerTodasLineasTransacciones();
        boolean any = false;
        for (String tx : txLines) {
            String[] p = tx.split("\\|");
            if (p.length < 5) continue;
            String idTx = p[0];
            String num = p[1];
            if (!num.equals(numero)) continue;
            any = true;
            String tipo = p[2];
            String monto = p[3];
            String fecha = p[4];
            System.out.println("ID: " + idTx + " | Tipo: " + tipo + " | Monto: " + monto + " | Fecha: " + fecha);
        }
        if (!any) System.out.println("No hay movimientos registrados para la cuenta " + numero);
    }
}

