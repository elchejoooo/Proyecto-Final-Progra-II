package Servicios;

import java.util.ArrayList;
import java.util.List;

import Modelos.Transaccion;
import Modelos.Cuenta;
import Excepciones.CuentaNoEncontradaExcepcion;
import Excepciones.PinInvalidoExcepcion;


public class ATM 
{
    private List<Transaccion> listaTransacciones;
    private java.util.Map<String, Cuenta> cuentas;
    private Cuenta cuentaActiva; // sesión actual
    // removed attempt-tracking: PIN attempts are not limited

    public ATM()
    {
        this.listaTransacciones = new ArrayList<>();
        this.cuentas = new java.util.HashMap<>();
        this.cuentaActiva = null;
    // no attempt tracking to initialize
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
     * Valida PIN para operaciones puntuales (no inicia sesión).
     * Si el PIN es incorrecto lanza PinInvalidoExcepcion; no hay conteo ni bloqueo.
     */
    private void validarPinParaOperacion(String numeroCuenta, String pin)
    {
        Cuenta cuentaPorValidar = this.cuentas.get(numeroCuenta);
        if (cuentaPorValidar == null)
            throw new CuentaNoEncontradaExcepcion(numeroCuenta);

        if (!cuentaPorValidar.getPin().equals(pin)) {
            throw new PinInvalidoExcepcion("PIN inválido.");
        }
    }

    /**
     * Verifica el PIN pero sin contar intentos ni bloquear la cuenta.
     * Devuelve true si el PIN es correcto, false si es incorrecto o la cuenta no existe.
     */
    public boolean verificarPinSinBloqueo(String numeroCuenta, String pin) {
        Cuenta cuentaPorValidar = this.cuentas.get(numeroCuenta);
        if (cuentaPorValidar == null) return false;
        return cuentaPorValidar.getPin().equals(pin);
    }

    /**
     * Devuelve true si la cuenta existe en el ATM.
     */
    public boolean existeCuenta(String numeroCuenta) {
        if (numeroCuenta == null) return false;
        if (this.cuentas.containsKey(numeroCuenta)) return true;
        // Si no está en memoria, comprobar archivo de control por si fue creado manualmente
        try {
            java.util.Map<String, String> lines = Reportes.leerTodasLineasCuentas();
            return lines.containsKey(numeroCuenta);
        } catch (Exception ex) {
            return false;
        }
    }

    public void cerrarSesion()
    {
        this.cuentaActiva = null;
    }

    public boolean estaAutenticado()
    {
        return this.cuentaActiva != null;
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
        Cuenta cuentaParaDepositar = this.cuentas.get(numeroCuenta);
        if (cuentaParaDepositar == null)
            throw new CuentaNoEncontradaExcepcion(numeroCuenta);

        Transaccion transaccionDeposito = new Transaccion(Enums.TipoTransaccion.DEPOSITO, monto, numeroCuenta, idTransaccion);//aqui se crea la transaccion al enviar los datos del tipo de transaccion, monto, numero de cuenta y id unico
        cuentaParaDepositar.aplicarDeposito(monto, transaccionDeposito);
        agregarTransaccion(transaccionDeposito);
    // actualizar archivo de control de cuentas y anexar transaccion
    try { Reportes.guardarTodasCuentas(new ArrayList<>(this.cuentas.values())); } catch (Exception ex) { }
    try { Reportes.appendTransaccion(transaccionDeposito); } catch (Exception ex) { }
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
        Cuenta cuentaPorRetirar = this.cuentas.get(numeroCuenta);
        if (cuentaPorRetirar == null)
            throw new CuentaNoEncontradaExcepcion(numeroCuenta);//verifica que la cuenta exista, sino lanza la excepcion

        Transaccion transaccionRetiro = new Transaccion(Enums.TipoTransaccion.RETIRO, monto, numeroCuenta, idTransaccion);//se envian daytos de la trnasaccion, se crea la transaccion
        cuentaPorRetirar.aplicarRetiro(monto, transaccionRetiro);
        agregarTransaccion(transaccionRetiro);
    // actualizar archivo de control de cuentas y anexar transaccion
    try { Reportes.guardarTodasCuentas(new ArrayList<>(this.cuentas.values())); } catch (Exception ex) { }
    try { Reportes.appendTransaccion(transaccionRetiro); } catch (Exception ex) { }
    }

    /**
     * Retirar tras validar PIN del titular (operación puntual sin iniciar sesión).
     */
    public void retirarConPin(String numeroCuenta, String pin, double monto, String idTransaccion)
    {
        validarPinParaOperacion(numeroCuenta, pin);//valida el pin antes de realizar la operacion
        retirar(numeroCuenta, monto, idTransaccion);//realiza el retiro, si falla lanza excepcion
    }

    /**
     * Devuelve el saldo actual de la cuenta indicada.
     */
    public double consultarSaldo(String numeroCuenta)
    {
        Cuenta cuentaConsultaSaldo = this.cuentas.get(numeroCuenta);
        if (cuentaConsultaSaldo == null)//verifica que la cuenta exista, sino lanza la excepcion
            throw new CuentaNoEncontradaExcepcion(numeroCuenta);
        return cuentaConsultaSaldo.getSaldo();
    }

    /**
     * Consultar saldo tras validar PIN del titular (operación puntual).
     */
    public double consultarSaldoConPin(String numeroCuenta, String pin)
    {
        validarPinParaOperacion(numeroCuenta, pin);//valida el pin antes de realizar la operacion, enviamos numero de cuenta y pin
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
        Transaccion transaccionRetiro = new Transaccion(Enums.TipoTransaccion.RETIRO, monto, numeroOrigen, idTransaccionOrigen);//aplica el retiro en la cuenta de origen
        origen.aplicarRetiro(monto, transaccionRetiro);//se aplica el retiro a la e origen
        this.agregarTransaccion(transaccionRetiro);

        Transaccion transaccionDeposito = new Transaccion(Enums.TipoTransaccion.DEPOSITO, monto, numeroDestino, idTransaccionDestino);
        destino.aplicarDeposito(monto, transaccionDeposito);//aplica deposito a la de destino
        this.agregarTransaccion(transaccionDeposito);
    // actualizar archivo de control de cuentas y anexar transacciones
    try { Reportes.guardarTodasCuentas(new ArrayList<>(this.cuentas.values())); } catch (Exception ex) { }
    try { Reportes.appendTransaccion(transaccionRetiro); } catch (Exception ex) { }
    try { Reportes.appendTransaccion(transaccionDeposito); } catch (Exception ex) { }
    }

    /**
     * Transferir tras validar PIN del titular de la cuenta origen (operación puntual sin iniciar sesión).
     */
    public void transferirConPin(String numeroOrigen, String pinOrigen, String numeroDestino, double monto, String idTransaccionOrigen, String idTransaccionDestino)
    {
        validarPinParaOperacion(numeroOrigen, pinOrigen);//valida el pin de la cuenta de origen antes de realizar la operacion
        transferir(numeroOrigen, numeroDestino, monto, idTransaccionOrigen, idTransaccionDestino);//realiza la transferencia, si falla lanza excepcion
    }

    public void eliminarTransaccion(Transaccion transaccion)
    {
        this.listaTransacciones.remove(transaccion);// elimina la transaccion del registro
    }
    
    public Transaccion buscarTransaccionPorId(String idTransaccion)
    {
        for (Transaccion transaccionBuscar : listaTransacciones) 
        {
            if (transaccionBuscar.getIdTransaccion().equals(idTransaccion)) // si encuentra la transaccion con el id buscado
            {
                return transaccionBuscar;
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

        for (java.util.Map.Entry<String, java.util.List<Transaccion>> entrada : porCuenta.entrySet()) {// aqui se recorre cada entrada del hashmap
            String numeroCuenta = entrada.getKey();// se obtiene el numero de la cuenta
            java.util.List<Transaccion> transacciones = entrada.getValue();// se obtiene la lista de transacciones de esa cuenta

            System.out.println("====================================");
            System.out.println("Resumen para la cuenta: " + numeroCuenta);
            System.out.println("Número de transacciones: " + transacciones.size());

            // Mostrar cada transacción usando el método de la clase Transaccion
            for (Transaccion transaccionMostrar : transacciones) {
                System.out.println(transaccionMostrar.mostrarDetallesTransaccion());
            }
        }
    }

    
    

}
