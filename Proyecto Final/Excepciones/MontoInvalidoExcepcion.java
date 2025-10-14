package Excepciones;

/**
 * Excepción personalizada para montos inválidos en las transacciones.
 */
public class MontoInvalidoExcepcion extends RuntimeException 
{
    public MontoInvalidoExcepcion(String mensaje) 
    {
        super(mensaje);
    }
}