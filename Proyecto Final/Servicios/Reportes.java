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
    //los archivos son static final, ya que no van a cambiar a lo largo de la ejecucion
    public static final String CONTROL_FILE = "ControlCuentas.txt";
    public static final String CONTROL_CLIENTES_FILE = "ControlClientes.txt";
    public static final String CONTROL_TRANS_FILE = "HistorialTransacciones.txt";

    /** Guarda todas las cuentas en el archivo ControlCuentas.txt con formato: numero|titular|tipo|saldo */
    public static void guardarTodasCuentas(List<Cuenta> cuentas) {
        File file = new File(CONTROL_FILE);
        // Sobrescribir el archivo exactamente con la lista actual (refleja eliminaciones)
        //bufferwriter es lo que nos va a permitir la escritura en el txt, es flexible
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, false))) {
            for (Cuenta cuenta : cuentas) {
                // Nuevo formato: numero|titular|idCliente|tipo|pin|saldo
                String linea = cuenta.getNumeroCuenta() + "|" + cuenta.getTitular().getNombreCompleto() + "|" + cuenta.getTitular().getIdCliente() + "|" + cuenta.getTipoCuenta() + "|" + cuenta.getPin() + "|" + String.format("%.2f", cuenta.getSaldo());
                writer.write(linea);
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error escribiendo ControlCuentas.txt: " + e.getMessage());
        }
    }

    /** Guarda todos los clientes en ControlClientes.txt con formato: idCliente|nombreCompleto|telefono|fechaNacimiento */
    public static void guardarTodosClientes(List<Modelos.Cliente> clientes) {
        File file = new File(CONTROL_CLIENTES_FILE);
        // Sobrescribir el archivo exactamente con la lista actual (refleja eliminaciones)
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, false))) {
            if (clientes != null) {// verificar que la lista no sea nula
                for (Modelos.Cliente cliente : clientes) {// recorrer cada cliente
                    String linea = cliente.getIdCliente() + "|" + cliente.getNombreCompleto() + "|" + cliente.getTelefono() + "|" + cliente.getFechaNacimiento();// crear la linea con los datos del cliente
                    writer.write(linea); writer.newLine();
                }
            }
        } catch (IOException e) { System.out.println("Error escribiendo ControlClientes.txt: " + e.getMessage()); }// manejar excepcion
    }

    /** Lee ControlClientes.txt y devuelve un map id->linea (no instancia objetos Cliente aquí) */
    public static java.util.Map<String, String> leerTodasLineasClientes() {
        java.util.Map<String, String> existentes = new java.util.LinkedHashMap<>();// mantener orden de insercion
        File file = new File(CONTROL_CLIENTES_FILE);//verifica que el archivo exista
        if (!file.exists()) return existentes;//si no existe, devuelve mapa vacio
        try (BufferedReader read = new BufferedReader(new FileReader(file))) {//lee el archivo
            String linea;
            while ((linea = read.readLine()) != null) {//lee cada linea
                String[] parts = linea.split("\\|");//separa por |
                if (parts.length >= 1) existentes.put(parts[0], linea);//usa el primer campo como clave (idCliente) y la linea completa como valor
            }
        } catch (IOException e) { System.out.println("Error leyendo ControlClientes.txt: " + e.getMessage()); }
        return existentes;
    }

    /** Lee ControlCuentas.txt y devuelve un map numeroCuenta->linea (no instancia objetos Cuenta aquí) */
    public static java.util.Map<String, String> leerTodasLineasCuentas() {
        java.util.Map<String, String> existentes = new java.util.LinkedHashMap<>();// mantener orden de insercion
        File file = new File(CONTROL_FILE);//verifica que el archivo exista
        if (!file.exists()) return existentes;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {//lee el archivo
            String linea;
            while ((linea = reader.readLine()) != null) {//lee cada linea
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
        File file = new File(CONTROL_TRANS_FILE);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, false))) {
            for (Modelos.Transaccion transac : transacciones) {
                String linea = transac.getIdTransaccion() + "|" + transac.getNumeroCuenta() + "|" + transac.getTipoTransaccion() + "|" + String.format("%.2f", transac.getMonto()) + "|" + transac.getFechaHora().toString();
                writer.write(linea); writer.newLine();
            }
        } catch (IOException e) { System.out.println("Error escribiendo HistorialTransacciones.txt: " + e.getMessage()); }
    }

    /** Agrega (append) una sola transacción al historial (útil para operaciones en vivo) */
    public static void appendTransaccion(Modelos.Transaccion transac) {
        File file = new File(CONTROL_TRANS_FILE);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            String linea = transac.getIdTransaccion() + "|" + transac.getNumeroCuenta() + "|" + transac.getTipoTransaccion() + "|" + String.format("%.2f", transac.getMonto()) + "|" + transac.getFechaHora().toString();
            writer.write(linea); writer.newLine();
        } catch (IOException e) { System.out.println("Error anexando a HistorialTransacciones.txt: " + e.getMessage()); }
    }

    /** Lee todas las transacciones del archivo y devuelve la lista de líneas (no instancia objetos aquí) */
    public static java.util.List<String> leerTodasLineasTransacciones() {
        java.util.List<String> out = new java.util.ArrayList<>();
        File file = new File(CONTROL_TRANS_FILE);
        if (!file.exists()) return out;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String linea;
            while ((linea = reader.readLine()) != null) out.add(linea);
        } catch (IOException e) { System.out.println("Error leyendo HistorialTransacciones.txt: " + e.getMessage()); }
        return out;
    }

    /** Lee ControlCuentas.txt y muestra cuentas con saldo menor al umbral */
    public static void imprimirCuentasConSaldoMenor(double umbral) {
        File file = new File(CONTROL_FILE);//verifica que el archivo exista
        if (!file.exists()) {
            System.out.println("Archivo de control no existe: " + CONTROL_FILE);
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {//lee el archivo, maneja excepcion, cierra recurso
            String linea;
            boolean cualquierCuenta = false;
            while ((linea = reader.readLine()) != null) {
                String[] parte = linea.split("\\|");//en esta parte separamos la linea por |, el \\ es para escapar el caracter especial
                // aceptar formatos antiguos (4 partes), previos (5 partes) o nuevo (6 partes con pin)
                if (parte.length < 4) continue;
                String numero = parte[0];
                String titular = parte[1];
                String idCliente = "";
                String tipo = "";
                String saldoStr = "0";
                if (parte.length == 4) {
                    // antiguo: numero|titular|tipo|saldo
                    tipo = parte[2];
                    saldoStr = parte[3];
                } else if (parte.length == 5) {
                    // intermedio: numero|titular|idCliente|tipo|saldo
                    idCliente = parte[2];
                    tipo = parte[3];
                    saldoStr = parte[4];
                } else {
                    // nuevo: numero|titular|idCliente|tipo|pin|saldo
                    idCliente = parte[2];
                    tipo = parte[3];
                    // parts[4] es pin, no se usa ya que no es necesario para este reporte
                    saldoStr = parte[5];
                }
                // normalizar coma decimal a punto
                saldoStr = saldoStr.replace(',', '.').trim();// en caso de que usen coma como separador decimal
                double saldo = 0.0;
                try { saldo = Double.parseDouble(saldoStr); } catch (NumberFormatException ex) { continue; }// si no es un numero valido, saltar
                if (saldo < umbral) {
                    cualquierCuenta = true;// marcar que se encontro al menos una cuenta
                    String extra = idCliente.isEmpty() ? "" : (" | ID: " + idCliente);
                    System.out.println("Cuenta: " + numero + " | Titular: " + titular + extra + " | Tipo: " + tipo + " | Saldo: " + String.format("%.2f", saldo));
                }
            }
            if (!cualquierCuenta) System.out.println("No se encontraron cuentas con saldo menor a " + String.format("%.2f", umbral));
        } catch (IOException e) {
            System.out.println("Error leyendo ControlCuentas.txt: " + e.getMessage());
        }
    }

    /** Menu simple para reportes (actualmente solo consulta saldos bajos) */
    public void menuReportes() {
        System.out.println("\nMenu de Reportes:\n1) Consulta Cuentas con Saldos Bajos\n2) Movimientos de cuenta especifica\n3) Historial Movimientos del Dia\n4) Volver");
        String opcion= Utilitaria.ScannerUtil.capturarTexto("Elija una opción de reportes:");
        if (opcion == null) return;
        opcion = opcion.trim();
        switch (opcion) {
            case "1":
                imprimirCuentasConSaldoMenor(200.00);
                break;
            case "2":
                mostrarMovimientosCuenta();
                break;
            case "3":
                mostrarMovimientosDelDia();
                break;
            default:
                // volver
                break;
        }
    }

    /** Muestra movimientos de una cuenta específica pidiendo número y PIN */
    public void mostrarMovimientosCuenta() {
    String numero = Utilitaria.ValidacionEntrada.solicitarCuentaValida(new Servicios.ATM(), "Ingrese número de cuenta:");
    if (numero == null) return;
    // obtener linea y pin del archivo
    java.util.Map<String,String> cuentas = leerTodasLineasCuentas();
    String linea = cuentas.get(numero);
    String[] parte = linea.split("\\|");
    String pin = (parte.length >= 6) ? parte[4] : "";

    String pinIngresado = Utilitaria.ScannerUtil.capturarTextoCancelable("Ingrese PIN para la cuenta " + numero + ":");
    if (pinIngresado == null) return;
    if (!pinIngresado.equals(pin)) { System.out.println("PIN inválido."); return; }

        // leer transacciones y filtrar por cuenta
        java.util.List<String> txLines = leerTodasLineasTransacciones();
        boolean cualquierTransaccion = false;
        for (String tx : txLines) {
            String[] parteDeTransaccion = tx.split("\\|");
            if (parteDeTransaccion.length < 5) continue; // linea tx mal formada -> saltar
            String idTx = parteDeTransaccion[0];
            String num = parteDeTransaccion[1];
            if (!num.equals(numero)) continue;
            cualquierTransaccion = true;
            String tipo = parteDeTransaccion[2];
            String monto = parteDeTransaccion[3];
            String fecha = parteDeTransaccion[4];
            System.out.println("ID: " + idTx + " | Tipo: " + tipo + " | Monto: " + monto + " | Fecha: " + fecha);
        }
        if (!cualquierTransaccion) System.out.println("No hay movimientos registrados para la cuenta " + numero);
    }

    /** Muestra todas las transacciones realizadas hoy (fecha local) */
    public void mostrarMovimientosDelDia() {
        java.util.List<String> txLines = leerTodasLineasTransacciones();
        java.time.LocalDate hoy = java.time.LocalDate.now();
        boolean cualquierMovimiento = false;
        for (String line : txLines) {
            try {
                String[] parteMovimientoDia = line.split("\\|");
                if (parteMovimientoDia.length < 5) continue;
                String idTx = parteMovimientoDia[0];
                String num = parteMovimientoDia[1];
                String tipo = parteMovimientoDia[2];
                String monto = parteMovimientoDia[3];
                java.time.LocalDateTime fecha = java.time.LocalDateTime.parse(parteMovimientoDia[4]);
                if (fecha.toLocalDate().equals(hoy)) {
                    cualquierMovimiento = true;
                    System.out.println("ID: " + idTx + " | Cuenta: " + num + " | Tipo: " + tipo + " | Monto: " + monto + " | Fecha: " + fecha.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
                }
            } catch (Exception ex) { /* ignorar lineas mal formadas */ }
        }
        if (!cualquierMovimiento) System.out.println("No se encontraron movimientos para hoy.");
    }
}

