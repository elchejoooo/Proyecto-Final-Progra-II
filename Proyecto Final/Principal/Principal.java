package Principal;

//import Enums.TipoCuenta;
import Modelos.Cliente;

public class Principal 
{
   public static void main(String[] args) 
   {
   // iniciar servicios
   Servicios.ATM atm = new Servicios.ATM();
   Servicios.Administrativo admin = new Servicios.Administrativo(atm);
   // cargar datos persistidos después de construir el objeto para evitar 'this' escape
   try { admin.cargarDesdeArchivos(); } catch (Exception ex) { /* ignore load errors */ }

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
                           String numero = Utilitaria.ValidacionEntrada.solicitarCuentaValida(atm, "Ingrese número de cuenta (o 'salir' para cancelar):");
                           if (numero == null) break innerLoop;
                           // Pedir PIN sin límite de intentos; si ingresa 'salir' se cancela
                           while (true) {
                              String pin = capturarSinEspacios("Ingrese PIN para la cuenta " + numero + " (o 'salir' para cancelar):");
                              if (pin == null) { System.out.println("Operación cancelada."); break; }
                              // verificar sin incrementar contadores ni bloquear
                              if (atm.verificarPinSinBloqueo(numero, pin)) {
                                 double saldo = atm.consultarSaldo(numero);
                                 System.out.println("Saldo: " + String.format("%.2f", saldo));
                                 break;
                              } else {
                                 System.out.println("PIN incorrecto. Regresando al menú de operaciones.");
                                 break; // regresar al menu de operaciones sin bloqueo
                              }
                           }
                        } catch (RuntimeException e) {
                           System.out.println("Error: " + e.getMessage());
                        }
                     break;
                  case "2": // depositar
                     try {
                        String numero = Utilitaria.ValidacionEntrada.solicitarCuentaValida(atm, "Ingrese número de cuenta para depositar (o 'salir' para cancelar):");
                        if (numero == null) { System.out.println("Operación cancelada."); break; }
                        Double montoObj = Utilitaria.ScannerUtil.capturarDoubleCancelable("Ingrese monto a depositar:");
                        if (montoObj == null) { System.out.println("Operación cancelada."); break; }
                        double monto = montoObj;
                        String pin = capturarSinEspacios("Ingrese PIN para autorizar el depósito:");
                        if (pin == null) { System.out.println("Operación cancelada."); break; }
                        String idTx = "DEP_" + System.currentTimeMillis();
                        atm.depositarConPin(numero, pin, monto, idTx);
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
                        String numero = Utilitaria.ValidacionEntrada.solicitarCuentaValida(atm, "Ingrese número de cuenta para retirar (o 'salir' para cancelar):");
                        if (numero == null) { System.out.println("Operación cancelada."); break; }
                        Double montoObj = Utilitaria.ScannerUtil.capturarDoubleCancelable("Ingrese monto a retirar:");
                        if (montoObj == null) { System.out.println("Operación cancelada."); break; }
                        double monto = montoObj;
                        String pin = capturarSinEspacios("Ingrese PIN para autorizar el retiro:");
                        if (pin == null) { System.out.println("Operación cancelada."); break; }
                        String idTx = "RET_" + System.currentTimeMillis();
                        atm.retirarConPin(numero, pin, monto, idTx);
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
                        String origen = Utilitaria.ValidacionEntrada.solicitarCuentaValida(atm, "Ingrese número de cuenta origen (o 'salir' para cancelar):");
                        if (origen == null) { System.out.println("Operación cancelada."); break; }
                        String destino = Utilitaria.ValidacionEntrada.solicitarCuentaValida(atm, "Ingrese número de cuenta destino (o 'salir' para cancelar):");
                        if (destino == null) { System.out.println("Operación cancelada."); break; }
                        Double montoObj = Utilitaria.ScannerUtil.capturarDoubleCancelable("Ingrese monto a transferir:");
                        if (montoObj == null) { System.out.println("Operación cancelada."); break; }
                        double monto = montoObj;
                        String pin = capturarSinEspacios("Ingrese PIN para autorizar la transferencia:");
                        if (pin == null) { System.out.println("Operación cancelada."); break; }
                        String idOrig = "TR_ORIG_" + System.currentTimeMillis();
                        String idDest = "TR_DST_" + System.currentTimeMillis();
                        atm.transferirConPin(origen, pin, destino, monto, idOrig, idDest);
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
                        String nombre = Utilitaria.ScannerUtil.capturarTextoCancelable("Ingrese nombre completo:");
                        if (nombre == null) { System.out.println("Operación cancelada."); break; }
                        String telefono = Utilitaria.ScannerUtil.capturarTextoCancelable("Ingrese teléfono:");
                        if (telefono == null) { System.out.println("Operación cancelada."); break; }
                        String fechaStr = Utilitaria.ScannerUtil.capturarTextoCancelable("Ingrese fecha de nacimiento (YYYY-MM-DD):");
                        if (fechaStr == null) { System.out.println("Operación cancelada."); break; }
                        java.time.LocalDate fecha = java.time.LocalDate.parse(fechaStr);
                        Cliente nuevo = admin.crearClienteAuto(nombre, telefono, fecha);
                        System.out.println("Cliente creado: " + nuevo.getNombreCompleto() + " (ID: " + nuevo.getIdCliente() + ")");
                     } catch (RuntimeException e) {
                        System.out.println("Error: " + e.getMessage());
                     }
                     break;
                  case "2": // eliminar cliente
                     try {
                        String id = Utilitaria.ScannerUtil.capturarTextoCancelable("Ingrese ID del cliente a eliminar:");
                        if (id == null) { System.out.println("Operación cancelada."); break; }
                        String confirma = Utilitaria.ScannerUtil.capturarTextoCancelable("Confirma eliminar el cliente " + id + "? (si/no):");
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
                        String idCliente = Utilitaria.ScannerUtil.capturarTextoCancelable("Ingrese ID del titular (para confirmar):");
                        if (idCliente == null) { System.out.println("Operación cancelada."); break; }
                        idCliente = idCliente.trim();
                        // verificar existencia en ControlClientes.txt antes de solicitar más datos
                        java.util.Map<String, String> clientesMap = Servicios.Reportes.leerTodasLineasClientes();
                        if (!clientesMap.containsKey(idCliente)) {
                            System.out.println("Cliente no encontrado en ControlClientes.txt. Regresando al menú de administración.");
                            break; // volver al menú administrativo sin pedir más campos
                        }
                        String tipoStr = Utilitaria.ScannerUtil.capturarTextoCancelable("Ingrese tipo de cuenta (AHORRO/MONETARIA):");
                        if (tipoStr == null) { System.out.println("Operación cancelada."); break; }
                        Enums.TipoCuenta tipo = Enums.TipoCuenta.valueOf(tipoStr.toUpperCase());
                        admin.crearCuentaPorIdAuto(tipo, idCliente);
                     } catch (RuntimeException e) {
                        System.out.println("Error: " + e.getMessage());
                     }
                     break;
                  case "4": // eliminar cuenta
                     try {
                        String numero = Utilitaria.ValidacionEntrada.solicitarCuentaValida(atm, "Ingrese número de cuenta a eliminar (o 'salir' para cancelar):");
                        if (numero == null) { System.out.println("Operación cancelada."); break; }
                        String confirmaC = Utilitaria.ScannerUtil.capturarTextoCancelable("Confirma eliminar la cuenta " + numero + "? (si/no):");
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

   /**
    * Captura texto sin espacios en blanco, repitiendo el prompt hasta que se ingrese un valor válido o se cancele.
    * Este metodo sirve para capturar entradas como PINs donde no se permiten espacios. Porque si el usuario ingresa espacios, se le pide que ingrese nuevamente.
    * Es util ya que en PINs no se permiten espacios.
    */
   private static String capturarSinEspacios(String prompt) {
      return capturarSinEspacios(prompt, null);//esto indica que no hay palabra especial permitida
      //eso significa que el usuario no puede ingresar ninguna palabra especial como por ejemplo la palabra 'volver' o 'cancelar'
   }

   private static String capturarSinEspacios(String prompt, String allowIfEquals) {
      while (true) {
         String entrada = Utilitaria.ScannerUtil.capturarTexto(prompt);
         if (entrada == null) return null; // usuario canceló osea no ingreso nada
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
