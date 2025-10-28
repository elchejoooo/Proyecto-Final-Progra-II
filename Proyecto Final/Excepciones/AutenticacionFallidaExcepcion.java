package Excepciones;

/**
     * Esta se activa cuando el PIN ingresado es incorrecto.
     * 
     */
public class AutenticacionFallidaExcepcion extends RuntimeException 
{
    private static final long serialVersionUID = 1L;
    
    public AutenticacionFallidaExcepcion(String cuenta) 
    {
        super("Falló la autenticación para la cuenta " + cuenta + ". PIN incorrecto.");
    }
}
