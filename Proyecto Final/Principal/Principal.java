package Principal;

//import Enums.TipoCuenta;
import Modelos.Cliente;

public class Principal 
{
   public static void main(String[] args) 
   {
   // iniciar servicios
   Servicios.ATM atm = new Servicios.ATM();
   Servicios.Administrativo admin = new Servicios.Administrativo(atm, true);

   mainLoop://mainloop es una etiqueta para poder salir de multiples while anidados
   //una etiqueta es un nombre que se le da a un bloque de codigo, en este caso a un while
   //para crear la etiqueta se escribe el nombre seguido de dos puntos, se debe usar break seguido del nombre de la etiqueta para salir
   // de todos los bloques anidados que estan dentro del bloque con etiqueta
   while (true) {
   System.out.println("\nMenú principal:\n1) Operaciones con cuenta\n2) Administrar cuentas\n3) Salir\n4) Visualizar Reportes");
      String mainOption = Utilitaria.ScannerUtil.capturarTexto("Elija una opción del menú principal:");
      if (mainOption == null) break;
      mainOption = mainOption.trim();

      switch (mainOption) {
         case "1": // Operaciones con cuenta
            System.out.println("\nOperaciones por demanda: ingrese su cuenta y PIN antes de cada operación.");
            innerLoop://se llama inerloop ya que es algo dentro del mainloop, este es para salir del menu de operaciones y regresar al menu principal
            while (true) {
               System.out.println("\nMenú de operaciones: \n1) Consultar saldo \n2) Depositar \n3) Retirar \n4) Transferir \n5) Volver");
               String opcion = Utilitaria.ScannerUtil.capturarTexto("Elija una opción del menú de operaciones:");
               if (opcion == null) break mainLoop;//esto indica que si el usuario ingresa null en cualquier menu, se sale del programa
               opcion = opcion.trim();//con trim nos aseguramos que no haya espacios al inicio o al final

               switch (opcion) {
                  case "1": // consultar saldo
                     try {
                        String numero = capturarSinEspacios("Ingrese número de cuenta (o 'volver'):", "volver");
                        if (numero == null) break mainLoop;//rompe el mainLoop si es null
                        numero = numero.trim();
                        if (numero.equalsIgnoreCase("volver")) break innerLoop;// se rompe el innerLoop si el usuario ingresa "volver"
                        String pin = capturarSinEspacios("Ingrese PIN para la cuenta " + numero + ":");
                        double saldo = atm.consultarSaldoConPin(numero, pin);//aqui consultamos el saldo usando el metodo del ATM enviando numero y pin
                        //el metodo consultarSaldoConPin lanza excepciones si hay error, por eso se usa try-catch
                        //si no hay error, se imprime el saldo
                        System.out.println("Saldo: " + String.format("%.2f", saldo));
                     } catch (RuntimeException e) {
                        System.out.println("Error: " + e.getMessage());
                     }
                     break;
                  case "2": // depositar
                     try {
                        String numero = capturarSinEspacios("Ingrese número de cuenta para depositar (o 'volver'):", "volver");
                        if (numero == null) break mainLoop;
                        numero = numero.trim();
                        if (numero.equalsIgnoreCase("volver")) break innerLoop;
                        String montoStr = Utilitaria.ScannerUtil.capturarTexto("Ingrese monto a depositar:");
                        double monto = Double.parseDouble(montoStr);
                        String pin = capturarSinEspacios("Ingrese PIN para autorizar el depósito:");
                        String idTx = "DEP_" + System.currentTimeMillis();//genera un ID unico basado en el tiempo actual 
                        atm.depositarConPin(numero, pin, monto, idTx);//aqui se llama al metodo depositarConPin del ATM
                        //el metodo depositarConPin lanza excepciones si hay error, por eso se usa try-catch
                        //si no hay error, se imprime el mensaje de exito
                        System.out.println("Depósito realizado. ID: " + idTx);//aqui se imprime el mensaje de exito, idTX es el id de la transaccion
                     } catch (NumberFormatException e) {
                        System.out.println("Error: formato de número inválido.");
                     } catch (RuntimeException e) {
                        System.out.println("Error: " + e.getMessage());
                     }
                     break;
                  case "3": // retirar
                     try {
                        String numero = capturarSinEspacios("Ingrese número de cuenta para retirar (o 'volver'):", "volver");
                        if (numero == null) break mainLoop;
                        numero = numero.trim();
                        if (numero.equalsIgnoreCase("volver")) break innerLoop;// si el usuario ingresa "volver", se sale del menu de operaciones y regresa al menu principal
                        String montoStr = Utilitaria.ScannerUtil.capturarTexto("Ingrese monto a retirar:");
                        double monto = Double.parseDouble(montoStr);//convierte el string a double
                        String pin = capturarSinEspacios("Ingrese PIN para autorizar el retiro:");
                        String idTx = "RET_" + System.currentTimeMillis();//genera un ID unico basado en el tiempo actual
                        atm.retirarConPin(numero, pin, monto, idTx);//aqui se llama al metodo retirarConPin del ATM
                        //el metodo retirarConPin lanza excepciones si hay error, por eso se usa try-catch
                        //si no hay error, se imprime el mensaje de exito
                        System.out.println("Retiro realizado. ID: " + idTx);//aqui se imprime el mensaje de exito, idTX es el id de la transaccion
                     } catch (NumberFormatException e) {
                        System.out.println("Error: formato de número inválido.");
                     } catch (RuntimeException e) {
                        System.out.println("Error: " + e.getMessage());
                     }
                     break;
                  case "4": // transferir
                     try {
                        String origen = capturarSinEspacios("Ingrese número de cuenta origen (o 'volver'):", "volver");
                        if (origen == null) break mainLoop;//si es null, se sale del programa
                        origen = origen.trim();
                        if (origen.equalsIgnoreCase("volver")) break innerLoop;// si el usuario ingresa "volver", se sale del menu de operaciones y regresa al menu principal
                        String destino = capturarSinEspacios("Ingrese número de cuenta destino:", "volver");
                        if (destino == null) break mainLoop;//si es null, se sale del programa
                        destino = destino.trim();
                        String montoStr = Utilitaria.ScannerUtil.capturarTexto("Ingrese monto a transferir:");
                        double monto = Double.parseDouble(montoStr);
                        String pin = capturarSinEspacios("Ingrese PIN para autorizar la transferencia:");
                        String idOrig = "TR_ORIG_" + System.currentTimeMillis();//genera un ID unico para la cuenta origen, tomado como retiro
                        String idDest = "TR_DST_" + System.currentTimeMillis();//genera un ID unico para la cuenta destino, tomado como deposito
                        atm.transferirConPin(origen, pin, destino, monto, idOrig, idDest);//aqui se llama al metodo transferirConPin del ATM se envia origen, pin, destino, monto, idOrig, idDest
                        //el metodo transferirConPin lanza excepciones si hay error, por eso se usa try-catch
                        //si no hay error, se imprime el mensaje de exito
                        System.out.println("Transferencia realizada. IDs: " + idOrig + ", " + idDest);
                     } catch (NumberFormatException e) {
                        System.out.println("Error: formato de número inválido.");
                     } catch (RuntimeException e) {
                        System.out.println("Error: " + e.getMessage());
                     }
                     break;
                  case "5":
                     break innerLoop;//aqui al seleccionar la opcion de volver, se sale del menu de operaciones y regresa al menu principal
                  default:
                     System.out.println("Opción no válida.");
               }
            }
            break;
         case "2": // Administrar cuentas
            adminLoop://adminloop es una etiqueta para salir del menu de administracion y regresar al menu principal
            //se usa otra etiqueta para salir del menu de administracion y regresar al menu principal
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
                        Cliente nuevo = admin.crearClienteAuto(nombre, telefono, fecha);//el ID se asigna automáticamente
                        System.out.println("Cliente creado: " + nuevo.getNombreCompleto() + " (ID: " + nuevo.getIdCliente() + ")");
                     } catch (RuntimeException e) {
                        System.out.println("Error: " + e.getMessage());
                     }
                     break;
                  case "2": // eliminar cliente
                     try {
                        String id = Utilitaria.ScannerUtil.capturarTexto("Ingrese ID del cliente a eliminar:");
                        String confirma = Utilitaria.ScannerUtil.capturarTexto("Confirma eliminar el cliente " + id + "? (si/no):");
                        if (confirma != null && confirma.trim().equalsIgnoreCase("si")) {
                           boolean ok = admin.eliminarCliente(id);
                           System.out.println(ok ? "Cliente eliminado." : "Cliente no encontrado.");//si ok es true, se imprime cliente eliminado, si es false, se imprime cliente no encontrado
                        } else {
                           System.out.println("Operación cancelada.");
                        }
                     } catch (RuntimeException e) {
                        System.out.println("Error: " + e.getMessage());
                     }
                     break;
                  case "3": // crear cuenta
                     try {
                        String idCliente = Utilitaria.ScannerUtil.capturarTexto("Ingrese ID del titular (para confirmar):");
                        String tipoStr = Utilitaria.ScannerUtil.capturarTexto("Ingrese tipo de cuenta (AHORRO/MONETARIA):");
                        Enums.TipoCuenta tipo = Enums.TipoCuenta.valueOf(tipoStr.toUpperCase());//convierte el string a enum, ignorando mayusculas o minusculas
                        admin.crearCuentaPorIdAuto(tipo, idCliente);
                     } catch (RuntimeException e) {
                        System.out.println("Error: " + e.getMessage());
                     }
                     break;
                  case "4": // eliminar cuenta
                     try {
                        String numero = capturarSinEspacios("Ingrese número de cuenta a eliminar:");
                        String confirmaC = Utilitaria.ScannerUtil.capturarTexto("Confirma eliminar la cuenta " + numero + "? (si/no):");
                        if (confirmaC != null && confirmaC.trim().equalsIgnoreCase("si")) {//si el usuario confirma que quiere eliminar la cuenta
                           boolean ok = admin.eliminarCuenta(numero);
                           System.out.println(ok ? "Cuenta eliminada." : "Cuenta no encontrada.");//si ok es true, se imprime cuenta eliminada, si es false, se imprime cuenta no encontrada
                        } else {
                           System.out.println("Operación cancelada.");
                        }
                     } catch (RuntimeException e) {
                        System.out.println("Error: " + e.getMessage());
                     }
                     break;
                  case "5":
                     break adminLoop;//aqui al seleccionar la opcion de volver, se sale del menu de administracion y regresa al menu principal
                  default:
                     System.out.println("Opción no válida.");
               }
            }
            break;
         case "3":
            System.out.println("Saliendo.");
            break mainLoop;//rompe el mainLoop para salir del programa
         case "4":
            // visualizar reportes
            try {
               Servicios.Reportes r = new Servicios.Reportes();//crea una instancia de la clase reportes
               r.menuReportes();//aqui se llama al metodo menuReportes de la clase reportes
            } catch (RuntimeException e) {
               System.out.println("Error mostrando reportes: " + e.getMessage());
            }
            break;
         default:
            System.out.println("Opción no válida.");
      }
   }


    
   }     

   // Helper: captura texto que no contenga espacios. Si se provee allowIfEquals, se permite
   // esa palabra especial (por ejemplo 'volver') incluso si el usuario la escribe con mayúsculas.
   private static String capturarSinEspacios(String prompt) {
      return capturarSinEspacios(prompt, null);//esto indica que no hay palabra especial permitida
      //eso significa que el usuario no puede ingresar ninguna palabra especial como por ejemplo la palabra 'volver' o 'cancelar'
   }

   private static String capturarSinEspacios(String prompt, String allowIfEquals) {
      while (true) {
         String entrada = Utilitaria.ScannerUtil.capturarTexto(prompt);
         if (entrada == null) return null; // usuario canceló (EOF) osea no ingreso nada
         //si el usuario ingresa null, se retorna null y se maneja en el menu principal
         //de esta forma si el usuario ingresa null en cualquier menu, se sale del programa
         String trimmed = entrada.trim();//aqui se quitan los espacios al inicio y al final
         if (allowIfEquals != null && trimmed.equalsIgnoreCase(allowIfEquals)) return trimmed;// en esta linea se permite la palabra especial como 'volver' o 'cancelar'
         //si la entrada es igual a la palabra especial permitida, se retorna la entrada sin importar mayusculas o minusculas
         if (trimmed.contains(" ")) {
            System.out.println("No use espacios en este campo. Intente nuevamente.");//no deja que un campo se deje en blanco
            continue;
         }
         return trimmed;
      }
   }

}
