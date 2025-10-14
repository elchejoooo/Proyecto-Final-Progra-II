package Excepciones;

/**
 * Excepción general para campos nulos, vacíos o inválidos.
 */
public class CampoInvalidoExcepcion extends RuntimeException 
{
    public CampoInvalidoExcepcion(String mensaje) 
    {
        super(mensaje);
    }
}