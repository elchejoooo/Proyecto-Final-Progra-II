package Excepciones;

/**
 * Excepción lanzada cuando una cuenta no tiene fondos suficientes para una operación.
 */
public class FondosInsuficientesExcepcion extends RuntimeException
{
    private static final long serialVersionUID = 1L;
    public FondosInsuficientesExcepcion(String cuenta) 
    {
        super("Fondos insuficientes en la cuenta: " + cuenta);
    }
}
