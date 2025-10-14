package Excepciones;

/**
 * Excepción personalizada que se lanza cuando el PIN de una cuenta es inválido.
 */
public class PinInvalidoExcepcion extends RuntimeException 
{
    public PinInvalidoExcepcion(String mensaje) 
    {
        super(mensaje);
    }
}