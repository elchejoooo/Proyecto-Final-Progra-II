package Servicios;

import java.util.ArrayList;
import java.util.List;

import Modelos.Transaccion;
import Modelos.Cuenta;
import Excepciones.CuentaNoEncontradaExcepcion;
import Excepciones.PinInvalidoExcepcion;
import Excepciones.SesionNoIniciadaExcepcion;

public class ATM 
{
    private List<Transaccion> listaTransacciones;
    private java.util.Map<String, Cuenta> cuentas;
    private Cuenta cuentaActiva; // sesión actual
    private java.util.Map<String, Integer> intentosFallidos;
    private final int MAX_INTENTOS = 3;

    public ATM()
    {
        this.listaTransacciones = new ArrayList<>();
        this.cuentas = new java.util.HashMap<>();
        this.cuentaActiva = null;
    this.intentosFallidos = new java.util.HashMap<>();
    }

    /**
     * Registra una cuenta en el ATM para poder autenticarla posteriormente.
     */
    public void registrarCuenta(Cuenta cuenta)
    {
        if (cuenta == null) return;
        this.cuentas.put(cuenta.getNumeroCuenta(), cuenta);
    }

    /**
     * Inicia sesión para la cuenta indicada si el PIN coincide. Lanza excepciones si falla.
     */
    public void iniciarSesion(String numeroCuenta, String pin)
    {
        Cuenta c = this.cuentas.get(numeroCuenta);
        if (c == null)
            throw new CuentaNoEncontradaExcepcion(numeroCuenta);

        int intentos = this.intentosFallidos.getOrDefault(numeroCuenta, 0);
        if (intentos >= MAX_INTENTOS) {
            throw new PinInvalidoExcepcion("La cuenta " + numeroCuenta + " está bloqueada por múltiples intentos fallidos.");
        }

        if (!c.getPin().equals(pin)) {
            intentos++;
            this.intentosFallidos.put(numeroCuenta, intentos);
            int restantes = MAX_INTENTOS - intentos;
            if (restantes <= 0) {
                throw new PinInvalidoExcepcion("PIN inválido. La cuenta ha sido bloqueada tras " + MAX_INTENTOS + " intentos fallidos.");
            } else {
                throw new PinInvalidoExcepcion("PIN inválido. Intentos restantes: " + restantes);
            }
        }

        // Autenticación exitosa: resetear contador de intentos y establecer sesión
        this.intentosFallidos.remove(numeroCuenta);
        this.cuentaActiva = c;
    }

    /**
     * Desbloquea los intentos fallidos para una cuenta (ej: administrador)
     */
    public void desbloquearCuenta(String numeroCuenta)
    {
        this.intentosFallidos.remove(numeroCuenta);
    }

    public int getIntentosFallidos(String numeroCuenta)
    {
        return this.intentosFallidos.getOrDefault(numeroCuenta, 0);
    }

    public void cerrarSesion()
    {
        this.cuentaActiva = null;
    }

    public boolean estaAutenticado()
    {
        return this.cuentaActiva != null;
    }

    public Cuenta getCuentaActiva()
    {
        if (!estaAutenticado())
            throw new SesionNoIniciadaExcepcion();
        return this.cuentaActiva;
    }

    public void agregarTransaccion(Transaccion transaccion)
    {
        this.listaTransacciones.add(transaccion);
    }

    public void eliminarTransaccion(Transaccion transaccion)
    {
        this.listaTransacciones.remove(transaccion);
    }
    
    public Transaccion buscarTransaccionPorId(String idTransaccion)
    {
        for (Transaccion t : listaTransacciones) 
        {
            if (t.getIdTransaccion().equals(idTransaccion)) 
            {
                return t;
            }
        }
        return null; // Si no
    }
    public void mostrarInformacionCuentas()
    {
        // Agrupar transacciones por número de cuenta y mostrar un resumen por cuenta
        java.util.Map<String, java.util.List<Transaccion>> porCuenta = new java.util.HashMap<>();

        for (Transaccion transaccion : listaTransacciones) {
            String numCuenta = transaccion.getNumeroCuenta();
            if (!porCuenta.containsKey(numCuenta)) {
                porCuenta.put(numCuenta, new ArrayList<>());
            }
            porCuenta.get(numCuenta).add(transaccion);
        }

        if (porCuenta.isEmpty()) {
            System.out.println("No hay transacciones registradas.");
            return;
        }

        for (java.util.Map.Entry<String, java.util.List<Transaccion>> entry : porCuenta.entrySet()) {
            String numeroCuenta = entry.getKey();
            java.util.List<Transaccion> transacciones = entry.getValue();

            System.out.println("====================================");
            System.out.println("Resumen para la cuenta: " + numeroCuenta);
            System.out.println("Número de transacciones: " + transacciones.size());

            // Mostrar cada transacción usando el método de la clase Transaccion
            for (Transaccion t : transacciones) {
                System.out.println(t.mostrarDetallesTransaccion());
            }
        }
    }
    
    
}
