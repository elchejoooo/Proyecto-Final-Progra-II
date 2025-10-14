package Excepciones;

/**
 * Excepción lanzada cuando una cuenta no tiene fondos suficientes para una operación.
 */
public class FondosInsuficientesExcepcion extends RuntimeException 
{
    public FondosInsuficientesExcepcion(String cuenta) 
    {
        super("Fondos insuficientes en la cuenta: " + cuenta);
    }
}
