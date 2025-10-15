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

        int intentos = this.intentosFallidos.getOrDefault(numeroCuenta, 0);//se obtienen los intentos fallidos actuales de esta cuenta
        if (intentos >= MAX_INTENTOS) {
            throw new PinInvalidoExcepcion("La cuenta " + numeroCuenta + " está bloqueada por múltiples intentos fallidos.");// si se falla 3 veces seguidas, la cuentase bloquea
        }

        if (!c.getPin().equals(pin)) {// si el pin es incorrecto entonces se aumenta el contador de fallos
            intentos++;
            this.intentosFallidos.put(numeroCuenta, intentos);
            int restantes = MAX_INTENTOS - intentos;
            if (restantes <= 0) {
                throw new PinInvalidoExcepcion("PIN inválido. La cuenta ha sido bloqueada tras " + MAX_INTENTOS + " intentos fallidos.");
            } else {
                throw new PinInvalidoExcepcion("PIN inválido. Intentos restantes: " + restantes);
            }
        }

        // al tener exito la autentificacion se resetea el contador de intentos y se inicia la sesion
        this.intentosFallidos.remove(numeroCuenta);
        this.cuentaActiva = c;//nos indica que la cuenta esta en sesion activa
    }

    /**
     * Desbloquea los intentos fallidos para una cuenta (ej: administrador)
     */
    public void desbloquearCuenta(String numeroCuenta)
    {
        this.intentosFallidos.remove(numeroCuenta);// resetea el contador de intentos fallidos
    }

    public int getIntentosFallidos(String numeroCuenta)
    {
        return this.intentosFallidos.getOrDefault(numeroCuenta, 0);
    }

    /**
     * Valida PIN para operaciones puntuales (no inicia sesión). Actualiza contador de intentos.
     */
    private void validarPinParaOperacion(String numeroCuenta, String pin)
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

        // PIN correcto: resetear contador
        this.intentosFallidos.remove(numeroCuenta);
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

    /**
     * Deposita un monto en la cuenta indicada. Registra la transacción y actualiza saldo.
     */
    public void depositar(String numeroCuenta, double monto, String idTransaccion)
    {
        Cuenta c = this.cuentas.get(numeroCuenta);
        if (c == null)
            throw new CuentaNoEncontradaExcepcion(numeroCuenta);

        Transaccion t = new Transaccion(Enums.TipoTransaccion.DEPOSITO, monto, numeroCuenta, idTransaccion);//aqui se crea la transaccion al enviar los datos del tipo de transaccion, monto, numero de cuenta y id unico
        c.aplicarDeposito(monto, t);
        agregarTransaccion(t);
    }

    /**
     * Depositar tras validar PIN del titular (operación puntual sin iniciar sesión).
     */
    public void depositarConPin(String numeroCuenta, String pin, double monto, String idTransaccion)
    {
        validarPinParaOperacion(numeroCuenta, pin);
        depositar(numeroCuenta, monto, idTransaccion);
    }

    /**
     * Retira un monto de la cuenta indicada. Registra la transacción y actualiza saldo.
     */
    public void retirar(String numeroCuenta, double monto, String idTransaccion)
    {
        Cuenta c = this.cuentas.get(numeroCuenta);
        if (c == null)
            throw new CuentaNoEncontradaExcepcion(numeroCuenta);

        Transaccion t = new Transaccion(Enums.TipoTransaccion.RETIRO, monto, numeroCuenta, idTransaccion);//se envian daytos de la trnasaccion, se crea la transaccion
        c.aplicarRetiro(monto, t);
        agregarTransaccion(t);
    }

    /**
     * Retirar tras validar PIN del titular (operación puntual sin iniciar sesión).
     */
    public void retirarConPin(String numeroCuenta, String pin, double monto, String idTransaccion)
    {
        validarPinParaOperacion(numeroCuenta, pin);
        retirar(numeroCuenta, monto, idTransaccion);
    }

    /**
     * Devuelve el saldo actual de la cuenta indicada.
     */
    public double consultarSaldo(String numeroCuenta)
    {
        Cuenta c = this.cuentas.get(numeroCuenta);
        if (c == null)//verifica que la cuenta exista, sino lanza la excepcion
            throw new CuentaNoEncontradaExcepcion(numeroCuenta);
        return c.getSaldo();
    }

    /**
     * Consultar saldo tras validar PIN del titular (operación puntual).
     */
    public double consultarSaldoConPin(String numeroCuenta, String pin)
    {
        validarPinParaOperacion(numeroCuenta, pin);
        return consultarSaldo(numeroCuenta);
    }

    /**
     * Transfiere monto de una cuenta a otra (si ambas existen y hay fondos suficientes).
     */
    public void transferir(String numeroOrigen, String numeroDestino, double monto, String idTransaccionOrigen, String idTransaccionDestino)
    {
        Cuenta origen = this.cuentas.get(numeroOrigen);//se obtiene la cuenta que envia los fondos
        Cuenta destino = this.cuentas.get(numeroDestino);//se obtiene la cuenta que recibe los fondos
        if (origen == null)// si no hay cuenta de origen, salta la excepcion
            throw new CuentaNoEncontradaExcepcion(numeroOrigen);
        if (destino == null)//ni no hay cuenta de destino, salta la excepcion
            throw new CuentaNoEncontradaExcepcion(numeroDestino);

        // Retiro de origen y depósito en destino con transacciones separadas
        Transaccion tRetiro = new Transaccion(Enums.TipoTransaccion.RETIRO, monto, numeroOrigen, idTransaccionOrigen);//aplica el retiro en la cuenta de origen
        origen.aplicarRetiro(monto, tRetiro);//se aplica el retiro a la e origen
        this.agregarTransaccion(tRetiro);

        Transaccion tDep = new Transaccion(Enums.TipoTransaccion.DEPOSITO, monto, numeroDestino, idTransaccionDestino);
        destino.aplicarDeposito(monto, tDep);//aplica deposito a la de destino
        this.agregarTransaccion(tDep);
    }

    /**
     * Transferir tras validar PIN del titular de la cuenta origen (operación puntual sin iniciar sesión).
     */
    public void transferirConPin(String numeroOrigen, String pinOrigen, String numeroDestino, double monto, String idTransaccionOrigen, String idTransaccionDestino)
    {
        validarPinParaOperacion(numeroOrigen, pinOrigen);
        transferir(numeroOrigen, numeroDestino, monto, idTransaccionOrigen, idTransaccionDestino);
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
        // aqui agrupamos las transacciones con respecto a su numero de cuenta
        java.util.Map<String, java.util.List<Transaccion>> porCuenta = new java.util.HashMap<>();

        for (Transaccion transaccion : listaTransacciones) {
            String numCuenta = transaccion.getNumeroCuenta();// se obtiene el numero de cuenta de la transaccion
            if (!porCuenta.containsKey(numCuenta)) {// si no existe la cuenta en el hashmap, se crea una  nueva entrada con una lista vacia 
                porCuenta.put(numCuenta, new ArrayList<>());
            }
            porCuenta.get(numCuenta).add(transaccion);// se agrega la transaccion a la lista de esa cuenta
        }// cada cuenta tiene su lista de transacciones asociada

        if (porCuenta.isEmpty()) {// si no hay transacciones, se le hace saber al usuario
            System.out.println("No hay transacciones registradas.");
            return;
        }

        for (java.util.Map.Entry<String, java.util.List<Transaccion>> entry : porCuenta.entrySet()) {// aqui se recorre cada entrada del hashmap
            String numeroCuenta = entry.getKey();// se obtiene el numero de la cuenta
            java.util.List<Transaccion> transacciones = entry.getValue();// se obtiene la lista de transacciones de esa cuenta

            System.out.println("====================================");
            System.out.println("Resumen para la cuenta: " + numeroCuenta);
            System.out.println("Número de transacciones: " + transacciones.size());

            // Mostrar cada transacción usando el método de la clase Transaccion
            for (Transaccion t : transacciones) {
                System.out.println(t.mostrarDetallesTransaccion());
            }
        }
    }

    
    // Operaciones convenientes que usan la cuenta autenticada
    public void retiroAutenticado(double monto, String idTransaccion) {
        if (!estaAutenticado())
            throw new SesionNoIniciadaExcepcion();

        String numero = getCuentaActiva().getNumeroCuenta();
        retirar(numero, monto, idTransaccion);
    }

    public void depositoAutenticado(double monto, String idTransaccion) {
        if (!estaAutenticado())
            throw new SesionNoIniciadaExcepcion();

        String numero = getCuentaActiva().getNumeroCuenta();
        depositar(numero, monto, idTransaccion);
    }

    public double consultarSaldoAutenticado() {
        if (!estaAutenticado())
            throw new SesionNoIniciadaExcepcion();

        return consultarSaldo(getCuentaActiva().getNumeroCuenta());
    }

    public void transferirAutenticado(String numeroDestino, double monto, String idTransaccionOrigen, String idTransaccionDestino) {
        if (!estaAutenticado())
            throw new SesionNoIniciadaExcepcion();

        String numeroOrigen = getCuentaActiva().getNumeroCuenta();
        transferir(numeroOrigen, numeroDestino, monto, idTransaccionOrigen, idTransaccionDestino);
    }

}
