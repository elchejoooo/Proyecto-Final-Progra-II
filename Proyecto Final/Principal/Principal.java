package Principal;

import java.time.LocalDate;

//import Enums.TipoCuenta;
import Modelos.Cliente;
import Modelos.Cuenta;

public class Principal 
{
   public static void main(String[] args) 
   {
   // registro rapido de un cliente, una cuenta y un par de transacciones
   Cliente cliente = new Cliente("12345678", "Juan Pérez", "555-1234", LocalDate.of(1990, 5, 15));
   System.out.println(cliente.mostrarDetallesCliente());

   // creamos una cuenta para el cliente ya creado 
   Enums.TipoCuenta tipoCuenta = Enums.TipoCuenta.AHORRO;
   Cuenta cuenta = new Cuenta("000123456789", "1234", tipoCuenta, cliente);
   cliente.agregarCuenta(cuenta);
   System.out.println(cuenta.mostrarDetallesCuenta());

   // invocamos a la clase ATM para poder trabajar con las transacciones
   Servicios.ATM atm = new Servicios.ATM();
   Servicios.Administrativo admin = new Servicios.Administrativo(atm);
   Enums.TipoTransaccion deposito = Enums.TipoTransaccion.DEPOSITO;
   Enums.TipoTransaccion retiro = Enums.TipoTransaccion.RETIRO;

   // colocamos unas transacciones manuales para probar el metodo mostrarInformacionCuentas
   Modelos.Transaccion t1 = new Modelos.Transaccion(deposito, 1500.00, cuenta.getNumeroCuenta(), "TXN001");
   Modelos.Transaccion t2 = new Modelos.Transaccion(retiro, 200.00, cuenta.getNumeroCuenta(), "TXN002");
   Modelos.Transaccion t3 = new Modelos.Transaccion(deposito, 500.00, "999888777666", "TXN003"); // otra cuenta ficticia

   //se agregan las transacciones al atm al numero de cuenta creado
   atm.agregarTransaccion(t1);
   atm.agregarTransaccion(t2);
   atm.agregarTransaccion(t3);

   System.out.println("\nMostrando información agrupada por cuentas desde el ATM:");
   atm.mostrarInformacionCuentas();

   // Registramos la cuenta creada y otra adicional para pruebas de transferencia
   atm.registrarCuenta(cuenta);
   Cuenta cuenta2 = new Cuenta("000987654321", "4321", tipoCuenta, cliente);
   cliente.agregarCuenta(cuenta2);
   atm.registrarCuenta(cuenta2);

   System.out.println("\nMenú principal:\n1) Operaciones con cuenta\n2) Administrar cuentas\n3) Salir");

   mainLoop:
   while (true) {
      System.out.println("\nMenú principal:\n1) Operaciones con cuenta\n2) Administrar cuentas\n3) Salir");
      String mainOption = Utilitaria.ScannerUtil.capturarTexto("Elija una opción del menú principal:");
      if (mainOption == null) break;
      mainOption = mainOption.trim();

      switch (mainOption) {
         case "1": // Operaciones con cuenta
            System.out.println("\nOperaciones por demanda: ingrese su cuenta y PIN antes de cada operación.");
            innerLoop:
            while (true) {
               System.out.println("\nMenú de operaciones: \n1) Consultar saldo \n2) Depositar \n3) Retirar \n4) Transferir \n5) Volver");
               String opcion = Utilitaria.ScannerUtil.capturarTexto("Elija una opción del menú de operaciones:");
               if (opcion == null) break mainLoop;
               opcion = opcion.trim();

               switch (opcion) {
                  case "1": // consultar saldo
                     try {
                        String numero = Utilitaria.ScannerUtil.capturarTexto("Ingrese número de cuenta (o 'volver'):");
                        if (numero == null) break mainLoop;
                        numero = numero.trim();
                        if (numero.equalsIgnoreCase("volver")) break innerLoop;
                        String pin = Utilitaria.ScannerUtil.capturarTexto("Ingrese PIN para la cuenta " + numero + ":");
                        double saldo = atm.consultarSaldoConPin(numero, pin);
                        System.out.println("Saldo: " + String.format("%.2f", saldo));
                     } catch (RuntimeException e) {
                        System.out.println("Error: " + e.getMessage());
                     }
                     break;
                  case "2": // depositar
                     try {
                        String numero = Utilitaria.ScannerUtil.capturarTexto("Ingrese número de cuenta para depositar (o 'volver'):");
                        if (numero == null) break mainLoop;
                        numero = numero.trim();
                        if (numero.equalsIgnoreCase("volver")) break innerLoop;
                        String montoStr = Utilitaria.ScannerUtil.capturarTexto("Ingrese monto a depositar:");
                        double monto = Double.parseDouble(montoStr);
                        String pin = Utilitaria.ScannerUtil.capturarTexto("Ingrese PIN para autorizar el depósito:");
                        String idTx = "DEP_" + System.currentTimeMillis();
                        atm.depositarConPin(numero, pin, monto, idTx);
                        System.out.println("Depósito realizado. ID: " + idTx);
                     } catch (NumberFormatException e) {
                        System.out.println("Error: formato de número inválido.");
                     } catch (RuntimeException e) {
                        System.out.println("Error: " + e.getMessage());
                     }
                     break;
                  case "3": // retirar
                     try {
                        String numero = Utilitaria.ScannerUtil.capturarTexto("Ingrese número de cuenta para retirar (o 'volver'):");
                        if (numero == null) break mainLoop;
                        numero = numero.trim();
                        if (numero.equalsIgnoreCase("volver")) break innerLoop;
                        String montoStr = Utilitaria.ScannerUtil.capturarTexto("Ingrese monto a retirar:");
                        double monto = Double.parseDouble(montoStr);
                        String pin = Utilitaria.ScannerUtil.capturarTexto("Ingrese PIN para autorizar el retiro:");
                        String idTx = "RET_" + System.currentTimeMillis();
                        atm.retirarConPin(numero, pin, monto, idTx);
                        System.out.println("Retiro realizado. ID: " + idTx);
                     } catch (NumberFormatException e) {
                        System.out.println("Error: formato de número inválido.");
                     } catch (RuntimeException e) {
                        System.out.println("Error: " + e.getMessage());
                     }
                     break;
                  case "4": // transferir
                     try {
                        String origen = Utilitaria.ScannerUtil.capturarTexto("Ingrese número de cuenta origen (o 'volver'):");
                        if (origen == null) break mainLoop;
                        origen = origen.trim();
                        if (origen.equalsIgnoreCase("volver")) break innerLoop;
                        String destino = Utilitaria.ScannerUtil.capturarTexto("Ingrese número de cuenta destino:");
                        if (destino == null) break mainLoop;
                        destino = destino.trim();
                        String montoStr = Utilitaria.ScannerUtil.capturarTexto("Ingrese monto a transferir:");
                        double monto = Double.parseDouble(montoStr);
                        String pin = Utilitaria.ScannerUtil.capturarTexto("Ingrese PIN para autorizar la transferencia:");
                        String idOrig = "TR_ORIG_" + System.currentTimeMillis();
                        String idDest = "TR_DST_" + System.currentTimeMillis();
                        atm.transferirConPin(origen, pin, destino, monto, idOrig, idDest);
                        System.out.println("Transferencia realizada. IDs: " + idOrig + ", " + idDest);
                     } catch (NumberFormatException e) {
                        System.out.println("Error: formato de número inválido.");
                     } catch (RuntimeException e) {
                        System.out.println("Error: " + e.getMessage());
                     }
                     break;
                  case "5":
                     break innerLoop;
                  default:
                     System.out.println("Opción no válida.");
               }
            }
            break;
         case "2": // Administrar cuentas
            adminLoop:
            while (true) {
               System.out.println("\nAdministrar cuentas: \n1) Crear cliente \n2) Eliminar cliente \n3) Crear cuenta \n4) Eliminar cuenta \n5) Volver");
               String aOpt = Utilitaria.ScannerUtil.capturarTexto("Elija una opción administrativa:");
               if (aOpt == null) break mainLoop;
               aOpt = aOpt.trim();

               switch (aOpt) {
                  case "1": // crear cliente
                     try {
                        String nombre = Utilitaria.ScannerUtil.capturarTexto("Ingrese nombre completo:");
                        String telefono = Utilitaria.ScannerUtil.capturarTexto("Ingrese teléfono:");
                        String fechaStr = Utilitaria.ScannerUtil.capturarTexto("Ingrese fecha de nacimiento (YYYY-MM-DD):");
                        java.time.LocalDate fecha = java.time.LocalDate.parse(fechaStr);
                        Cliente nuevo = admin.crearClienteAuto(nombre, telefono, fecha);
                        System.out.println("Cliente creado: " + nuevo.getNombreCompleto() + " (ID: " + nuevo.getIdCliente() + ")");
                     } catch (RuntimeException e) {
                        System.out.println("Error: " + e.getMessage());
                     }
                     break;
                  case "2": // eliminar cliente
                     try {
                        String id = Utilitaria.ScannerUtil.capturarTexto("Ingrese ID del cliente a eliminar:");
                        boolean ok = admin.eliminarCliente(id);
                        System.out.println(ok ? "Cliente eliminado." : "Cliente no encontrado.");
                     } catch (RuntimeException e) {
                        System.out.println("Error: " + e.getMessage());
                     }
                     break;
                  case "3": // crear cuenta
                     try {
                        String pin = Utilitaria.ScannerUtil.capturarTexto("Ingrese PIN (4 dígitos):");
                        String tipoStr = Utilitaria.ScannerUtil.capturarTexto("Ingrese tipo de cuenta (AHORRO/MONETARIA):");
                        Enums.TipoCuenta tipo = Enums.TipoCuenta.valueOf(tipoStr.toUpperCase());
                        String nombreTitular = Utilitaria.ScannerUtil.capturarTexto("Ingrese nombre completo del titular (tal como está registrado):");
                        admin.crearCuentaAuto(pin, tipo, nombreTitular);
                     } catch (RuntimeException e) {
                        System.out.println("Error: " + e.getMessage());
                     }
                     break;
                  case "4": // eliminar cuenta
                     try {
                        String numero = Utilitaria.ScannerUtil.capturarTexto("Ingrese número de cuenta a eliminar:");
                        boolean ok = admin.eliminarCuenta(numero);
                        System.out.println(ok ? "Cuenta eliminada." : "Cuenta no encontrada.");
                     } catch (RuntimeException e) {
                        System.out.println("Error: " + e.getMessage());
                     }
                     break;
                  case "5":
                     break adminLoop;
                  default:
                     System.out.println("Opción no válida.");
               }
            }
            break;
         case "3":
            System.out.println("Saliendo.");
            break mainLoop;
         default:
            System.out.println("Opción no válida.");
      }
   }


    
   }     
}
