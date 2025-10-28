package Excepciones;

/**
 * Excepción personalizada para montos inválidos en las transacciones.
 */
public class MontoInvalidoExcepcion extends RuntimeException
{
    private static final long serialVersionUID = 1L;
    public MontoInvalidoExcepcion(String mensaje) 
    {
        super(mensaje);
    }
}