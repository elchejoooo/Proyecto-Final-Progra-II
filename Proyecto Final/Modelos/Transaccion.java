package Modelos;

import Enums.TipoTransaccion;
import Excepciones.CampoInvalidoExcepcion;
import Excepciones.MontoInvalidoExcepcion;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Transaccion 
{
    // Atributos
    private TipoTransaccion tipoTransaccion;
    private double monto;
    private LocalDateTime fechaHora;
    private String numeroCuenta; // La cuenta afectada

    // Constructor
    public Transaccion(TipoTransaccion tipoTransaccion, double monto, String numeroCuenta) 
    {
        validarCampos(tipoTransaccion, monto, numeroCuenta);

        this.tipoTransaccion = tipoTransaccion;
        this.monto = monto;
        this.numeroCuenta = numeroCuenta;
        this.fechaHora = LocalDateTime.now();
    }

    // Métodos
    public String mostrarDetallesTransaccion()
    {
        return "---------------\n" +
               "Tipo de Transacción: " + this.tipoTransaccion + 
               "\nMonto: " + String.format("%.2f", this.monto) + 
               "\nFecha y Hora: " + this.fechaHora.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) +
               "\nNúmero de Cuenta: " + this.numeroCuenta;
    }

    /**
     * Valida los campos antes de crear una transacción.
     */
    private void validarCampos(TipoTransaccion tipoTransaccion, double monto, String numeroCuenta)
    {
        if (tipoTransaccion == null)
            throw new CampoInvalidoExcepcion("El tipo de transacción no puede ser nulo.");

        if (numeroCuenta == null || numeroCuenta.trim().isEmpty())
            throw new CampoInvalidoExcepcion("El número de cuenta no puede estar vacío.");

        validarMonto(monto);
    }

    /**
     * Valida que el monto sea positivo y diferente de cero.
     */
    private void validarMonto(double monto)
    {
        if (monto <= 0)
            throw new MontoInvalidoExcepcion("El monto debe ser mayor que cero.");
    }

    // Getters y Setters con validaciones
    public TipoTransaccion getTipoTransaccion() {
        return tipoTransaccion;
    }

    public void setTipoTransaccion(TipoTransaccion tipoTransaccion) {
        if (tipoTransaccion == null)
            throw new CampoInvalidoExcepcion("El tipo de transacción no puede ser nulo.");
        this.tipoTransaccion = tipoTransaccion;
    }

    public double getMonto() {
        return monto;
    }

    public void setMonto(double monto) {
        validarMonto(monto);
        this.monto = monto;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    // La fecha de la transacción es inmodificable
    protected void setFechaHora(LocalDateTime fechaHora) {
        if (fechaHora == null)
            throw new CampoInvalidoExcepcion("La fecha de la transacción no puede ser nula.");
        this.fechaHora = fechaHora;
    }

    public String getNumeroCuenta() {
        return numeroCuenta;
    }

    public void setNumeroCuenta(String numeroCuenta) {
        if (numeroCuenta == null || numeroCuenta.trim().isEmpty())
            throw new CampoInvalidoExcepcion("El número de cuenta no puede estar vacío.");
        this.numeroCuenta = numeroCuenta;
    }
}
