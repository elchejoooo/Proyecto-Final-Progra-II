package Excepciones;

/**
 * Excepción lanzada cuando se intenta realizar una operación sin haber iniciado sesión.
 */
public class SesionNoIniciadaExcepcion extends RuntimeException
{
    private static final long serialVersionUID = 1L;
    public SesionNoIniciadaExcepcion() 
    {
        super("No hay sesión iniciada. Autentíquese antes de realizar operaciones.");
    }
}