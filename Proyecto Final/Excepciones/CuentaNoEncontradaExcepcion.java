package Excepciones;

/**
 * Excepción lanzada cuando no se encuentra una cuenta específica.
 */
public class CuentaNoEncontradaExcepcion extends RuntimeException 
{
    public CuentaNoEncontradaExcepcion(String numeroCuenta) 
    {
        super("No se encontró la cuenta con número: " + numeroCuenta);
    }
}