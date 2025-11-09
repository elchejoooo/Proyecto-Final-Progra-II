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

	/** Constructor alterno: carga clientes y cuentas desde archivos de control si existen */
	public Administrativo(ATM atm, boolean cargarDesdeArchivos) {
		this(atm);
		
	}

	/** aqui se cargan desde el archivo los clientes */
	public void cargarDesdeArchivos() {
		// Cargar clientes
		java.util.Map<String, String> clientesLines = Reportes.leerTodasLineasClientes();
		long maxId = 0L;//el id mas alto entre todos los clientes cargados, esto sirve para asegurar que el siguiente id generado sea mayor a este
		for (String linea : clientesLines.values()) {
			try {
				String[] parts = linea.split("\\|");
				// esperamos: idCliente|nombreCompleto|telefono|fechaNacimiento
				if (parts.length < 4) continue;
				String id = parts[0];
				String nombre = parts[1];
				String telefono = parts[2];
				java.time.LocalDate fecha = java.time.LocalDate.parse(parts[3]);
				Cliente clienteCargar = new Cliente(id, nombre, telefono, fecha);
				this.clientes.add(clienteCargar);
				try { long v = Long.parseLong(id); if (v > maxId) maxId = v; } catch (NumberFormatException ex) { }
			} catch (Exception ex) {
				// ignorar linea mal formada
				System.out.println("Error al cargar cliente: " + ex.getMessage());
			}
		}
		this.nextClienteId = Math.max(this.nextClienteId, maxId + 1);//aseguranos de que el siguiente id sea mayor al maximo ya cargado

		// Cargar cuentas
		java.util.Map<String, String> cuentasLines = Reportes.leerTodasLineasCuentas();
		long maxCuentaId = 0L;//el id mas alto entre las cuentas cargadas
		for (String line : cuentasLines.values()) {
			try {
				String[] parteCarga = line.split("\\|");
				if (parteCarga.length < 5) continue;
				String numero = parteCarga[0];
				String nombreTitular = parteCarga[1];
				String idCliente = parteCarga[2];
				Enums.TipoCuenta tipo = Enums.TipoCuenta.valueOf(parteCarga[3]);
				String pin = "0000";
				String saldoStr = "0";
				if (parteCarga.length == 5) {
					saldoStr = parteCarga[4].replace(',', '.');
				} else if (parteCarga.length >= 6) {
					pin = parteCarga[4];
					saldoStr = parteCarga[5].replace(',', '.');
				}
				double saldo = 0.0;
				try { saldo = Double.parseDouble(saldoStr); } catch (NumberFormatException e) { }
				Cliente titular = buscarClientePorId(idCliente);
				if (titular == null) {
					titular = new Cliente(idCliente, nombreTitular, "", java.time.LocalDate.of(1970,1,1));
					this.clientes.add(titular);
				}
				Cuenta cuentaCrear = crearCuenta(numero, pin, tipo, titular);
				// establecer saldo inicial sin crear transacciones importadas; las transacciones
				// reales se cargarán desde HistorialTransacciones.txt más abajo
				if (saldo > 0.0) {
					try { cuentaCrear.cargarSaldoInicial(saldo); } catch (Exception ex) 
					{ 
						System.out.println("Error al cargar saldo inicial para cuenta " + numero + ": " + ex.getMessage());
					}
				}
				try { long v = Long.parseLong(numero); if (v > maxCuentaId) maxCuentaId = v; } catch (NumberFormatException ex) { }
			} catch (Exception ex) 
			{
				// ignorar linea mal formada
				System.out.println("Error al cargar cuenta: " + ex.getMessage());
			}
		}
		this.nextCuentaId = Math.max(this.nextCuentaId, maxCuentaId + 1);

		// Cargar transacciones y asociarlas a cuentas/ATM
		java.util.List<String> txLines = Reportes.leerTodasLineasTransacciones();
		for (String line : txLines) 
		{
			try 
			{
				String[] parteLeerDesdeArchivos = line.split("\\|");
				if (parteLeerDesdeArchivos.length < 5) continue;
				String idTx = parteLeerDesdeArchivos[0];
				String numCuenta = parteLeerDesdeArchivos[1];
				Enums.TipoTransaccion tipo = Enums.TipoTransaccion.valueOf(parteLeerDesdeArchivos[2]);
				double monto = Double.parseDouble(parteLeerDesdeArchivos[3].replace(',', '.'));
				java.time.LocalDateTime fecha = java.time.LocalDateTime.parse(parteLeerDesdeArchivos[4]);
				Modelos.Transaccion transaccionLeer = new Modelos.Transaccion(tipo, monto, numCuenta, idTx, fecha);
				if (this.atm != null) this.atm.agregarTransaccion(transaccionLeer);
				Cuenta cuentaTransaccion = null;
				for (Cuenta cc : this.cuentas) if (cc.getNumeroCuenta().equals(numCuenta)) { cuentaTransaccion = cc; break; }
				if (cuentaTransaccion != null) {
					try { cuentaTransaccion.agregarTransaccion(transaccionLeer); } catch (Exception ex) { }
				}
			} catch (Exception ex) { }
		}
	}

	

	/** Crea y registra un cliente en memoria */
	public Cliente crearCliente(String idCliente, String nombreCompleto, String telefono, java.time.LocalDate fechaNacimiento)
	{
		// Validar que no exista otro cliente con mismo telefono y fechaNacimiento
		for (Cliente existente : this.clientes) 
		{
			if (existente.getTelefono().equals(telefono) && existente.getFechaNacimiento().equals(fechaNacimiento)) 
			{
				throw new RuntimeException("Ya existe un cliente con ese teléfono y fecha de nacimiento.");
			}
		}
		Cliente clienteCrear = new Cliente(idCliente, nombreCompleto, telefono, fechaNacimiento);//aqui se crea el cliente, se envia a el constructor de Cliente
		clientes.add(clienteCrear);
		try { Reportes.guardarTodosClientes(this.clientes); } catch (Exception ex) { }// no bloquear si falla el guardado, mas bien se intenta nuevamente en el futuro cuando se cree otro cliente
		return clienteCrear;
	}

	/** Genera un identificador único para un nuevo cliente. */
	public synchronized String generarIdCliente()//synchronized para evitar condiciones de carrera en entornos multihilo, lo que significa que solo un hilo puede ejecutar este método a la vez
	{// un hilo es una secuencia de ejecucion, en aplicaciones multihilo varios hilos pueden intentar ejecutar este metodo al mismo tiempo, lo que podria causar que dos clientes obtengan el mismo ID
		String id = String.valueOf(this.nextClienteId);
		this.nextClienteId++;
		return id;
	}

	/** Crea un cliente generando automáticamente su ID. */
	public Cliente crearClienteAuto(String nombreCompleto, String telefono, java.time.LocalDate fechaNacimiento)
	{
		// antes de generar id, validar duplicados por telefono+fecha
		for (Cliente existente : this.clientes) {
			if (existente.getTelefono().equals(telefono) && existente.getFechaNacimiento().equals(fechaNacimiento)) {
				throw new RuntimeException("Ya existe un cliente con ese teléfono y fecha de nacimiento.");
			}
		}
		String id = generarIdCliente();
		return crearCliente(id, nombreCompleto, telefono, fechaNacimiento);//aqui se crea el cliente con el id generado automaticamente
	}

	/** Busca un cliente por nombre completo (primer match). */
	public Cliente buscarClientePorNombre(String nombreCompleto)
	{
		if (nombreCompleto == null) return null;
		String buscado = nombreCompleto.trim();
		for (Cliente c : clientes) {
			if (c.getNombreCompleto().equalsIgnoreCase(buscado)) return c;//equalsIgnoreCase ignora mayusculas y minusculas
		}
		return null;
	}

	/** Genera un número de cuenta único (incremental). */
	public synchronized String generarNumeroCuenta()//synchronized significa que solo un hilo puede ejecutar este metodo a la vez
	{// el no tener synchronized podria causar que dos cuentas obtengan el mismo numero de cuenta en entornos multihilo
	// Generar un número de cuenta secuencial formateado a 12 dígitos (con ceros a la izquierda)
	String numero = String.format("%012d", this.nextCuentaId);//formatea el numero de cuenta a 12 digitos, si es menor se le agregan ceros a la izquierda
	this.nextCuentaId++;
	return numero;
	}

	/** Crea una cuenta generando automáticamente el número y buscando al titular por nombre. */
	public Cuenta crearCuentaAuto(String pin, Enums.TipoCuenta tipoCuenta, String nombreTitular)
	{
		Cliente titular = buscarClientePorNombre(nombreTitular);//buscar el cliente por su nombre
		if (titular == null)//si no se encuentra el cliente
			throw new RuntimeException("Titular no encontrado: " + nombreTitular);

		String numero = generarNumeroCuenta();
		return crearCuenta(numero, pin, tipoCuenta, titular);
	}

	/** Elimina un cliente por id (si existe) y sus cuentas asociadas */
	public boolean eliminarCliente(String idCliente)
	{
		Cliente objetivo = null;
		for (Cliente clientePorEliminar : clientes) {
			if (clientePorEliminar.getIdCliente().equals(idCliente)) { objetivo = clientePorEliminar; break; }//esto indica que se encontro el cliente a eliminar
		}
		if (objetivo == null) return false;

		// eliminar sus cuentas del ATM y de la lista
		for (Cuenta cu : new ArrayList<>(objetivo.getCuentas())) {//usar copia para evitar ConcurrentModificationException
			eliminarCuenta(cu.getNumeroCuenta());//aqui se elimina la cuenta del cliente
		}

		clientes.remove(objetivo);
		try { Reportes.guardarTodosClientes(this.clientes); } catch (Exception ex) { }// no bloquear si falla el guardado, mas bien se intenta nuevamente en el futuro cuando se elimine otro cliente
		return true;//indica que se elimino el cliente
	}

	/** Crea una cuenta asociada a un cliente existente */
	public Cuenta crearCuenta(String numeroCuenta, String pin, Enums.TipoCuenta tipoCuenta, Cliente titular)
	{
		// Validar que el titular no tenga ya una cuenta del mismo tipo
		for (Cuenta existing : titular.getCuentas()) {//recorre las cuentas del titular
			if (existing.getTipoCuenta() == tipoCuenta) {//si ya tiene una cuenta del mismo tipo
				throw new RuntimeException("El titular ya posee una cuenta del tipo: " + tipoCuenta);
			}
		}
		Cuenta cuenta = new Cuenta(numeroCuenta, pin, tipoCuenta, titular);
		cuentas.add(cuenta);
		// vincular a titular
		titular.agregarCuenta(cuenta);
		// registrar en ATM
		if (this.atm != null) this.atm.registrarCuenta(cuenta);
		// Nota: la visualizacion al crear la cuenta la realiza el llamador si corresponde
		// actualizar archivo de control de cuentas
	try { Reportes.guardarTodasCuentas(this.cuentas); } catch (Exception ex) { /* no bloquear */ }
		return cuenta;
	}

	/** Busca un cliente por su ID (que sea identico) */
	public Cliente buscarClientePorId(String idCliente) {
		if (idCliente == null) return null;
		for (Cliente clientePorBuscar : clientes) {
			if (clientePorBuscar.getIdCliente().equals(idCliente)) return clientePorBuscar;
		}
		return null;
	}

	/** Crea una cuenta generando automaticamente  y PIN, usando el idCliente para ubicar al titular. */
	public Cuenta crearCuentaPorIdAuto(Enums.TipoCuenta tipoCuenta, String idCliente) {
		Cliente titular = buscarClientePorId(idCliente);
		if (titular == null) throw new RuntimeException("Titular no encontrado con ID: " + idCliente);

		String numero = generarNumeroCuenta();
		// generar PIN aleatorio de 4 didgitos
		int pinInt = (int) (Math.random() * 9000) + 1000; // garantiza 1000-9999
		String pin = String.format("%04d", pinInt);

		Cuenta cuenta = crearCuenta(numero, pin, tipoCuenta, titular);

		// Mostrar informacion detallada al crear la cuenta 
		System.out.println("\nCuenta creada exitosamente:");
		System.out.println("Numero de cuenta: " + cuenta.getNumeroCuenta());
		System.out.println("Nombre del titular: " + titular.getNombreCompleto());
		System.out.println("Tipo de cuenta: " + tipoCuenta);
		System.out.println("Pin: " + pin + "\n");

		return cuenta;
	}

	/** Elimina una cuenta por número: la quita de listas y del ATM */
	public boolean eliminarCuenta(String numeroCuenta)
	{
		Cuenta objetivo = null;
		for (Cuenta cuentaPorEliminar : cuentas) {
			if (cuentaPorEliminar.getNumeroCuenta().equals(numeroCuenta)) { objetivo = cuentaPorEliminar; break; }//esto indica que se encontro la cuenta a eliminar
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
			// no hay método para desregistrar explícito, pero quitar de la lista local evita su uso
		}

		cuentas.remove(objetivo);
		// actualizar archivo de control
		try { Reportes.guardarTodasCuentas(this.cuentas); } catch (Exception ex) { }
		return true;
	}
}
