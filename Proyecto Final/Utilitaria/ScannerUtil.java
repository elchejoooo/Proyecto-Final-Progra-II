package Utilitaria;

import java.util.Scanner;

import Enums.TipoTransaccion;
import Enums.TipoCuenta;
public class ScannerUtil
{
    
    private static Scanner scanner = new Scanner(System.in);
    /**
     * este metodo nos ayuda a capturar un String del usuario
     * @param mensajeParaElUsuario es lo que se le muestra al usuario
     * @return nos devuelve el String capturado
     */
    public static String capturarTexto(String mensajeParaElUsuario)
    {
       System.out.println(mensajeParaElUsuario);
       return scanner.nextLine(); 
    }
    /**
     * este metodo captura un entero ingresado por el usuario
     * @param mensajeParaElUsuario es lo que se le muestra al usuario
     * @return nos devuelve el entero capturado
     */
    public static int capturarEntero(String mensajeParaElUsuario)
    {
            System.out.print(mensajeParaElUsuario);
            while (!scanner.hasNextInt()) 
            {
            System.out.println("Entrada inválida. Por favor, ingrese un número entero.");
            scanner.next(); 
            }
        int numero = scanner.nextInt();
        scanner.nextLine(); 
        return numero;
    }
    /**
     * este metodo nos ayuda a capturar un numero double ingresado por el usuario
     * @param mensajeParaElUsuario es lo que se le muestra al usuario
     * @return nos devuelve el double capturado
     */
    public static double capturarDouble(String mensajeParaElUsuario)
    {
          System.out.print(mensajeParaElUsuario);
            while (!scanner.hasNextInt()) 
            {
            System.out.println("Entrada inválida. Por favor, ingrese un número entero.");
            scanner.next(); 
            }
        double numero = scanner.nextDouble();
        scanner.nextLine(); 
        return numero;
    }
    /**
     * Este metodo nos ayuda a capturar un TipoTransaccion que ingrese el usuario.
     * Nos auxiliamos del enum TipoTransaccion.
     * Si se ingresa algo que no esta dentro de ese enum, se le vuelve a pedir al usuario que ingrese una opcion valida.
     * @return el TipoTransaccion que el usuario, ingreso y que este en enum TipoTransaccion
     * @param mensaje es lo que se le muestra al usuario
     */
    public static TipoTransaccion capturarTipoTransaccion(String mensaje) 
    {
        while (true) 
        {
            System.out.println(mensaje + "... Opciones:");
            for (TipoTransaccion tipoTransaccion : TipoTransaccion.values()) 
            {
                System.out.println("- " + tipoTransaccion.name());
            }
            String entrada = capturarTexto("Cual quieres?");
            try 
            {
                return TipoTransaccion.valueOf(entrada.toUpperCase());
            } catch (IllegalArgumentException e) 
            {
                System.out.println("Tipo de transaccion no encontrada: " + entrada + "; Intenta de nuevo.");
            }
        }
    }
    /**
     * Este metodo nos ayuda a capturar un TipoCuenta que ingrese el usuario.
     * Nos auxiliamos del enum TipoCuenta.
     * Si se ingresa algo que no esta dentro de ese enum, se le vuelve a pedir al usuario que ingrese una opcion valida.
     * @return el TipoCuenta que el usuario ingreso, y que este en enum TipoCuenta
     * @param mensaje es lo que se le muestra al usuario
     */
    public static TipoCuenta capturarTipoCuenta(String mensaje) 
    {
        while (true) 
        {
            System.out.println(mensaje + "... Opciones:");
            for (TipoCuenta tipoCuenta : TipoCuenta.values()) 
            {
                System.out.println("- " + tipoCuenta.name());
            }
            String entrada = capturarTexto("Cual quieres?");
            try 
            {
                return TipoCuenta.valueOf(entrada.toUpperCase());
            } catch (IllegalArgumentException e) 
            {
                System.out.println("Tipo de cuenta no encontrada: " + entrada + "; Intenta de nuevo.");
            }
        }
    }
}
