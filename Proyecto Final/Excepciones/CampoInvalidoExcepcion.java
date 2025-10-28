package Excepciones;

/**
 * Excepción general para campos nulos, vacíos o inválidos.
 */
public class CampoInvalidoExcepcion extends RuntimeException
{
    private static final long serialVersionUID = 1L;
    public CampoInvalidoExcepcion(String mensaje) 
    {
        super(mensaje);
    }
}