package Principal;

import java.time.LocalDate;

//import Enums.TipoCuenta;
import Modelos.Cliente;
import Modelos.Cuenta;

public class Principal 
{
   public static void main(String[] args) 
   {
   // Demo rápido para comprobar ATM.mostrarInformacionCuentas()
   Cliente cliente = new Cliente("12345678", "Juan Pérez", "555-1234", LocalDate.of(1990, 5, 15));
   System.out.println(cliente.mostrarDetallesCliente());

   // Crear una cuenta manualmente usando un tipo por defecto (AHORRO) si existe
   Enums.TipoCuenta tipoCuenta = Enums.TipoCuenta.AHORRO;
   Cuenta cuenta = new Cuenta("000123456789", "1234", tipoCuenta, cliente);
   cliente.agregarCuenta(cuenta);
   System.out.println(cuenta.mostrarDetallesCuenta());

   // Crear un ATM y agregar un par de transacciones para la misma cuenta y otra distinta
   Servicios.ATM atm = new Servicios.ATM();
   Enums.TipoTransaccion deposito = Enums.TipoTransaccion.DEPOSITO;
   Enums.TipoTransaccion retiro = Enums.TipoTransaccion.RETIRO;

   Modelos.Transaccion t1 = new Modelos.Transaccion(deposito, 1500.00, cuenta.getNumeroCuenta(), "TXN001");
   Modelos.Transaccion t2 = new Modelos.Transaccion(retiro, 200.00, cuenta.getNumeroCuenta(), "TXN002");
   Modelos.Transaccion t3 = new Modelos.Transaccion(deposito, 500.00, "999888777666", "TXN003"); // otra cuenta ficticia

   atm.agregarTransaccion(t1);
   atm.agregarTransaccion(t2);
   atm.agregarTransaccion(t3);

   System.out.println("\nMostrando información agrupada por cuentas desde el ATM:");
   atm.mostrarInformacionCuentas();

   // Demostración interactiva: el usuario ingresa el PIN por consola
   atm.registrarCuenta(cuenta); // registrar la cuenta para permitir autenticación
   System.out.println("\nAutenticación interactiva. Escriba 'salir' para terminar.");
   while (true) {
      String entrada = Utilitaria.ScannerUtil.capturarTexto("Ingrese PIN para la cuenta " + cuenta.getNumeroCuenta() + ":");
      if (entrada == null) break;
      entrada = entrada.trim();
      if (entrada.equalsIgnoreCase("salir")) {
         System.out.println("Saliendo del intento de autenticación.");
         break;
      }

      try {
         atm.iniciarSesion(cuenta.getNumeroCuenta(), entrada);
         System.out.println("Autenticación exitosa. Sesión iniciada para: " + atm.getCuentaActiva().getNumeroCuenta());
         atm.cerrarSesion();
         break; // salir tras autenticación exitosa
      } catch (RuntimeException ex) {
         System.out.println("Error: " + ex.getMessage());
         // continuar el loop para permitir otro intento o 'salir'
      }
   }


    
   }     
}
