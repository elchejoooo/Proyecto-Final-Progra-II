package Modelos;

import Excepciones.ClienteInvalidoExcepcion;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

public class Cliente 
{
    // Atributos
    private String idCliente;
    private String nombreCompleto;
    private String telefono;
    private LocalDate fechaNacimiento;
    private List<Cuenta> cuentas;

    // Constructor
    public Cliente(String idCliente, String nombreCompleto, String telefono, LocalDate fechaNacimiento) 
    {
        validarCampos(idCliente, nombreCompleto, telefono, fechaNacimiento);

        this.idCliente = idCliente;
        this.nombreCompleto = nombreCompleto;
        this.telefono = telefono;
        this.fechaNacimiento = fechaNacimiento;
        this.cuentas = new ArrayList<>();
    }

    // Métodos
    /**
     * Muestra los detalles básicos del cliente
     * @return una cadena con la información del cliente
     */
    public String mostrarDetallesCliente()
    {
        return "---------------\n" +
               "ID Cliente: " + this.idCliente + 
               "\nNombre Completo: " + this.nombreCompleto + 
               "\nTeléfono: " + this.telefono + 
               "\nFecha de Nacimiento: " + this.fechaNacimiento +
               "\nEdad: " + calcularEdad() + " años" +
               "\nNúmero de Cuentas: " + this.cuentas.size();
    }

    /**
     * Agrega una cuenta a la lista de cuentas del cliente
     * @param cuenta la cuenta que se va a agregar
     */
    public void agregarCuenta(Cuenta cuenta) 
    {
        if (cuenta == null)
            throw new ClienteInvalidoExcepcion("La cuenta a agregar no puede ser nula.");
        cuentas.add(cuenta);
    }

    /**
     * Busca una cuenta por su número de cuenta
     * @param numeroCuenta el número de cuenta que se va a buscar
     * @return la cuenta si se encuentra, null si no se encuentra
     */
    public Cuenta buscarCuenta(String numeroCuenta) 
    {
        for (Cuenta cuenta : cuentas) {
            if (cuenta.getNumeroCuenta().equals(numeroCuenta)) {
                return cuenta;
            }
        }
        return null;
    }

    /**
     * Calcula la edad actual del cliente a partir de su fecha de nacimiento
     * @return edad en años
     */
    public int calcularEdad() 
    {
        return Period.between(this.fechaNacimiento, LocalDate.now()).getYears();
    }

    /**
     * Verifica que los campos no estén vacíos y que la edad esté entre 18 y 100 años
     */
    private void validarCampos(String idCliente, String nombreCompleto, String telefono, LocalDate fechaNacimiento)
    {
        if (idCliente == null || idCliente.trim().isEmpty())
            throw new ClienteInvalidoExcepcion("El ID del cliente no puede estar vacío.");

        if (nombreCompleto == null || nombreCompleto.trim().isEmpty())
            throw new ClienteInvalidoExcepcion("El nombre completo no puede estar vacío.");

        if (telefono == null || telefono.trim().isEmpty())
            throw new ClienteInvalidoExcepcion("El número de teléfono no puede estar vacío.");

        if (fechaNacimiento == null)
            throw new ClienteInvalidoExcepcion("La fecha de nacimiento no puede ser nula.");

        int edad = Period.between(fechaNacimiento, LocalDate.now()).getYears();
        if (edad < 18)
            throw new ClienteInvalidoExcepcion("El cliente debe tener al menos 18 años.");
        if (edad > 100)
            throw new ClienteInvalidoExcepcion("La edad del cliente no puede superar los 100 años.");
    }

    // Getters y Setters con validación
    public String getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(String idCliente) {
        if (idCliente == null || idCliente.trim().isEmpty())
            throw new ClienteInvalidoExcepcion("El ID del cliente no puede estar vacío.");
        this.idCliente = idCliente;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        if (nombreCompleto == null || nombreCompleto.trim().isEmpty())
            throw new ClienteInvalidoExcepcion("El nombre completo no puede estar vacío.");
        this.nombreCompleto = nombreCompleto;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        if (telefono == null || telefono.trim().isEmpty())
            throw new ClienteInvalidoExcepcion("El número de teléfono no puede estar vacío.");
        this.telefono = telefono;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        if (fechaNacimiento == null)
            throw new ClienteInvalidoExcepcion("La fecha de nacimiento no puede ser nula.");

        int edad = Period.between(fechaNacimiento, LocalDate.now()).getYears();
        if (edad < 18)
            throw new ClienteInvalidoExcepcion("El cliente debe tener al menos 18 años.");
        if (edad > 100)
            throw new ClienteInvalidoExcepcion("La edad del cliente no puede superar los 100 años.");
        this.fechaNacimiento = fechaNacimiento;
    }

    public List<Cuenta> getCuentas() {
        return cuentas;
    }

    public void setCuentas(List<Cuenta> cuentas) {
        if (cuentas == null)
            throw new ClienteInvalidoExcepcion("La lista de cuentas no puede ser nula.");
        this.cuentas = cuentas;
    }
}