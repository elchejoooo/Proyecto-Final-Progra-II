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

   // aqui registramos una cuenta para poder tener un inicio de sesion
   atm.registrarCuenta(cuenta); 
   System.out.println("\nInicio de sesión interactivo. Escriba 'salir' como número de cuenta para terminar.");

   while (true) {// este while nos permite volver a intentar el inicio de sesion si fallamos inicialmente
      String numero = Utilitaria.ScannerUtil.capturarTexto("Ingrese número de cuenta:");
      if (numero == null) break;
      numero = numero.trim();
      if (numero.equalsIgnoreCase("salir")) {// si en la autentificacion escribimos salir, entonces se termina el programa
         System.out.println("Saliendo del programa.");
         break;
      }

      //aqui pedimimos el pin de la cuenta para iniciar la sesion
      String pin = Utilitaria.ScannerUtil.capturarTexto("Ingrese PIN para la cuenta " + numero + ":");
      if (pin == null) break;
      pin = pin.trim();

      try {// este try catch nos permite intentar iniciar sesion 3 veces como maximo
         atm.iniciarSesion(numero, pin);
      } catch (RuntimeException ex) {
         System.out.println("Fallo al iniciar sesión: " + ex.getMessage());
         continue; // permitir intentar nuevamente
      }

      System.out.println("\nSesión iniciada para: " + numero);

      // Menú simple
      boolean salirMenu = false;
      while (!salirMenu) {
         System.out.println("\nElija una opción: \n1) Consultar saldo \n2) Depositar \n3) Retirar \n4) Cerrar sesión");
         String opcion = Utilitaria.ScannerUtil.capturarTexto("Opción:");
         if (opcion == null) break;
         opcion = opcion.trim();

         switch (opcion) {// menu con las opciones de operacion que tiene el usuario en su cuenta
            case "1":
               try {
                  double saldo = atm.consultarSaldoAutenticado();
                  System.out.println("Saldo actual: " + String.format("%.2f", saldo));
               } catch (RuntimeException e) {
                  System.out.println("Error: " + e.getMessage());
               }
               break;
            case "2":
               try {
                  String montoStr = Utilitaria.ScannerUtil.capturarTexto("Ingrese monto a depositar:");
                  double monto = Double.parseDouble(montoStr);
                  String idTx = "DEP_" + System.currentTimeMillis();// id unico baso en su deposito ya que no puede haber 2 depositos al mismo tiempo, es un generador de id
                  atm.depositoAutenticado(monto, idTx);
                  System.out.println("Depósito realizado. ID transacción: " + idTx);
               } catch (NumberFormatException e) {
                  System.out.println("Error: formato de número inválido.");
               } catch (RuntimeException e) {
                  System.out.println("Error: " + e.getMessage());
               }
               break;
            case "3":
               try {
                  String montoStr = Utilitaria.ScannerUtil.capturarTexto("Ingrese monto a retirar:");
                  double monto = Double.parseDouble(montoStr);
                  String idTx = "RET_" + System.currentTimeMillis();// Id unico basado en el tiempo actual ya que no se repite debido a que no puede haber dos transacciones al mismo tiempo
                  atm.retiroAutenticado(monto, idTx);// esta linea hace el retiro basado en la cuenta que inicio sesion
                  System.out.println("Retiro realizado. ID transacción: " + idTx);//aqui se muestra el id generado de la transaccion
               } catch (NumberFormatException e) {
                  System.out.println("Error: formato de número inválido.");// esta salta si el usuario ingresa algo que no es un numero en el apartado de monto
               } catch (RuntimeException e) {
                  System.out.println("Error: " + e.getMessage());
               }
               break;
            case "4":
               atm.cerrarSesion();
               System.out.println("Sesión cerrada.");
               salirMenu = true;
               break;
            default:
               System.out.println("Opción no válida. Intente de nuevo.");
         }
      }

      // Si el usuario cerró sesión en el menú, puede iniciar otra o salir
   }


    
   }     
}
