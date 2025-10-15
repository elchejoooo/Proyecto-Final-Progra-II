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

   System.out.println("\nOperaciones por demanda: ingrese su cuenta y PIN antes de cada operación. Escriba 'salir' como número de cuenta para terminar.");

   mainLoop:
   while (true) {
      System.out.println("\nMenú principal: \n1) Consultar saldo \n2) Depositar \n3) Retirar \n4) Transferir \n5) Salir");
      String opcion = Utilitaria.ScannerUtil.capturarTexto("Elija una opción:");
      if (opcion == null) break;
      opcion = opcion.trim();

      switch (opcion) {
         case "1": // consultar saldo
            try {
               String numero = Utilitaria.ScannerUtil.capturarTexto("Ingrese número de cuenta (o 'salir'):");
               if (numero == null) break mainLoop;
               numero = numero.trim();
               if (numero.equalsIgnoreCase("salir")) break mainLoop;
               String pin = Utilitaria.ScannerUtil.capturarTexto("Ingrese PIN para la cuenta " + numero + ":");
               double saldo = atm.consultarSaldoConPin(numero, pin);
               System.out.println("Saldo: " + String.format("%.2f", saldo));
            } catch (RuntimeException e) {
               System.out.println("Error: " + e.getMessage());
            }
            break;
         case "2": // depositar
            try {
               String numero = Utilitaria.ScannerUtil.capturarTexto("Ingrese número de cuenta para depositar (o 'salir'):");
               if (numero == null) break mainLoop;
               numero = numero.trim();
               if (numero.equalsIgnoreCase("salir")) break mainLoop;
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
               String numero = Utilitaria.ScannerUtil.capturarTexto("Ingrese número de cuenta para retirar (o 'salir'):");
               if (numero == null) break mainLoop;
               numero = numero.trim();
               if (numero.equalsIgnoreCase("salir")) break mainLoop;
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
               String origen = Utilitaria.ScannerUtil.capturarTexto("Ingrese número de cuenta origen (o 'salir'):");
               if (origen == null) break mainLoop;
               origen = origen.trim();
               if (origen.equalsIgnoreCase("salir")) break mainLoop;
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
            System.out.println("Saliendo.");
            break mainLoop;
         default:
            System.out.println("Opción no válida.");
      }
   }


    
   }     
}
