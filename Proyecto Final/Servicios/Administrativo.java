package Servicios;

import Modelos.Cliente;
import Modelos.Cuenta;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase administrativa para crear/eliminar clientes y cuentas.
 */
public class Administrativo 
{
	private List<Cliente> clientes;
	private List<Cuenta> cuentas;
	private ATM atm; // referencia para registrar/desregistrar cuentas
	private long nextClienteId;
	private long nextCuentaId;

	public Administrativo(ATM atm)
	{
		this.clientes = new ArrayList<>();
		this.cuentas = new ArrayList<>();
		this.atm = atm;
	this.nextClienteId = 1L;
	this.nextCuentaId = 1L;
	}

	public List<Cliente> getClientes() { return clientes; }
	public List<Cuenta> getCuentas() { return cuentas; }

	/** Crea y registra un cliente en memoria */
	public Cliente crearCliente(String idCliente, String nombreCompleto, String telefono, java.time.LocalDate fechaNacimiento)
	{
		Cliente c = new Cliente(idCliente, nombreCompleto, telefono, fechaNacimiento);
		clientes.add(c);
		return c;
	}

	/** Genera un identificador único para un nuevo cliente. */
	public synchronized String generarIdCliente()
	{
		String id = String.valueOf(this.nextClienteId);
		this.nextClienteId++;
		return id;
	}

	/** Crea un cliente generando automáticamente su ID. */
	public Cliente crearClienteAuto(String nombreCompleto, String telefono, java.time.LocalDate fechaNacimiento)
	{
		String id = generarIdCliente();
		return crearCliente(id, nombreCompleto, telefono, fechaNacimiento);
	}

	/** Busca un cliente por nombre completo (primer match). */
	public Cliente buscarClientePorNombre(String nombreCompleto)
	{
		if (nombreCompleto == null) return null;
		String buscado = nombreCompleto.trim();
		for (Cliente c : clientes) {
			if (c.getNombreCompleto().equalsIgnoreCase(buscado)) return c;
		}
		return null;
	}

	/** Genera un número de cuenta único (incremental). */
	public synchronized String generarNumeroCuenta()
	{
	// Generar un número de cuenta secuencial formateado a 12 dígitos (con ceros a la izquierda)
	String numero = String.format("%012d", this.nextCuentaId);
	this.nextCuentaId++;
	return numero;
	}

	/** Crea una cuenta generando automáticamente el número y buscando al titular por nombre. */
	public Cuenta crearCuentaAuto(String pin, Enums.TipoCuenta tipoCuenta, String nombreTitular)
	{
		Cliente titular = buscarClientePorNombre(nombreTitular);
		if (titular == null)
			throw new RuntimeException("Titular no encontrado: " + nombreTitular);

		String numero = generarNumeroCuenta();
		return crearCuenta(numero, pin, tipoCuenta, titular);
	}

	/** Elimina un cliente por id (si existe) y sus cuentas asociadas */
	public boolean eliminarCliente(String idCliente)
	{
		Cliente objetivo = null;
		for (Cliente c : clientes) {
			if (c.getIdCliente().equals(idCliente)) { objetivo = c; break; }
		}
		if (objetivo == null) return false;

		// eliminar sus cuentas del ATM y de la lista
		for (Cuenta cu : new ArrayList<>(objetivo.getCuentas())) {
			eliminarCuenta(cu.getNumeroCuenta());
		}

		clientes.remove(objetivo);
		return true;
	}

	/** Crea una cuenta asociada a un cliente existente */
	public Cuenta crearCuenta(String numeroCuenta, String pin, Enums.TipoCuenta tipoCuenta, Cliente titular)
	{
		Cuenta cuenta = new Cuenta(numeroCuenta, pin, tipoCuenta, titular);
		cuentas.add(cuenta);
		// vincular a titular
		titular.agregarCuenta(cuenta);
		// registrar en ATM
		if (this.atm != null) this.atm.registrarCuenta(cuenta);
	// Mostrar información al crear la cuenta (único mensaje)
	System.out.println("Cuenta creada: " + cuenta.getNumeroCuenta() + " - Titular: " + titular.getNombreCompleto());
		return cuenta;
	}

	/** Elimina una cuenta por número: la quita de listas y del ATM */
	public boolean eliminarCuenta(String numeroCuenta)
	{
		Cuenta objetivo = null;
		for (Cuenta c : cuentas) {
			if (c.getNumeroCuenta().equals(numeroCuenta)) { objetivo = c; break; }//esto indica que se encontro la cuenta a eliminar
		}
		if (objetivo == null) return false;

		// quitar de su titular
		Cliente t = objetivo.getTitular();
		if (t != null) {
			t.getCuentas().removeIf(c -> c.getNumeroCuenta().equals(numeroCuenta));// aqui se quita la cuenta de la lista de cuentas del cliente
		}

		// quitar del ATM
		if (this.atm != null) {
			this.atm.cerrarSesion(); // asegurar que no hay sesión abierta con esa cuenta
			this.atm.desbloquearCuenta(numeroCuenta); // limpiar intentos
			// no hay método para desregistrar explícito, pero quitar de la lista local evita su uso
		}

		cuentas.remove(objetivo);
		return true;
	}
}
