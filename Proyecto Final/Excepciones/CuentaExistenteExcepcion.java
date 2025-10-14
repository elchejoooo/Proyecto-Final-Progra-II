package Excepciones;

/**
 * Excepción que se lanza cuando se intenta crear una cuenta que ya existe.
 */
public class CuentaExistenteExcepcion extends RuntimeException 
{
    public CuentaExistenteExcepcion(String numeroCuenta) 
    {
        super("La cuenta con número " + numeroCuenta + " ya existe.");
    }
}
