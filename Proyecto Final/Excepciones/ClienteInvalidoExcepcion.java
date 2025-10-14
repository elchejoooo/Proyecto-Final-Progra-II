package Excepciones;

/**
 * Excepción lanzada cuando los datos del cliente son inválidos.
 */
public class ClienteInvalidoExcepcion extends RuntimeException
{
    public ClienteInvalidoExcepcion(String mensaje)
    {
        super("Error en los datos del cliente: " + mensaje);
    }
}