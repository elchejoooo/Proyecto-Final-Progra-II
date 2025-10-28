package Excepciones;

/**
 * Excepción que se lanza cuando se intenta crear una cuenta que ya existe.
 */
public class CuentaExistenteExcepcion extends RuntimeException
{
    private static final long serialVersionUID = 1L;
    public CuentaExistenteExcepcion(String numeroCuenta) 
    {
        super("La cuenta con número " + numeroCuenta + " ya existe.");
    }
}
