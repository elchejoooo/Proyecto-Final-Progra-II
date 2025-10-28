package Excepciones;

/**
 * Excepción lanzada cuando los datos del cliente son inválidos.
 */
public class ClienteInvalidoExcepcion extends RuntimeException
{
    private static final long serialVersionUID = 1L;
    public ClienteInvalidoExcepcion(String mensaje)
    {
        super("Error en los datos del cliente: " + mensaje);
    }
}