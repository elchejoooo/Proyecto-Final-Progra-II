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
		if (!cargarDesdeArchivos) return;

		// Cargar clientes
		java.util.Map<String, String> clientesLines = Reportes.leerTodasLineasClientes();
		long maxId = 0L;
		for (String line : clientesLines.values()) {
			try {
				String[] parts = line.split("\\|");//split nos indica que separe la linea en partes donde haya un |
				// esperamos: idCliente|nombreCompleto|telefono|fechaNacimiento
				if (parts.length < 4) continue;//si la linea no tiene al menos 4 partes, se ignora
				String id = parts[0];
				String nombre = parts[1];
				String telefono = parts[2];
				java.time.LocalDate fecha = java.time.LocalDate.parse(parts[3]);//puede lanzar excepcion si el formato es invalido
				// crear cliente y agregar a la lista
				Cliente c = new Cliente(id, nombre, telefono, fecha);//aqui se crea el cliente con los datos obtenidos del archivo
				this.clientes.add(c);//aqui se agrega el cliente a la lista de clientes del administrativo
				try { long v = Long.parseLong(id); if (v > maxId) maxId = v; } catch (NumberFormatException ex) { }//aqui se obtiene el maximo id de cliente para luego asignar el siguiente, si el id no es numerico se ignora
				//Long.parseLong puede lanzar NumberFormatException si el id no es un numero valido
				// actualizar nextClienteId despues de procesar todos los clientes
			} catch (Exception ex) {
				// ignorar linea mal formada
			}
		}
		this.nextClienteId = Math.max(this.nextClienteId, maxId + 1);//asegurar que el siguiente id es mayor al maximo encontrado

		// Cargar cuentas
		java.util.Map<String, String> cuentasLines = Reportes.leerTodasLineasCuentas();//lee todas las lineas del archivo ControlCuentas.txt y las devuelve en un mapa
		long maxCuentaId = 0L;//0L ya que es un long porque los numeros de cuenta son largos
		for (String line : cuentasLines.values()) {
			try {
				String[] parts = line.split("\\|");
				// esperamos: numero|nombreTitular|idCliente|tipo|saldo  (pin no almacenado en este formato)
				if (parts.length < 5) continue;
				String numero = parts[0];
				String nombreTitular = parts[1];
				String idCliente = parts[2];
				Enums.TipoCuenta tipo = Enums.TipoCuenta.valueOf(parts[3]);
				String pin = "0000";// PIN por defecto si no se encuentra en el archivo
				String saldoStr = "0";// saldo por defecto
				if (parts.length == 5) {
					// old format without pin: numero|titular|idCliente|tipo|saldo
					saldoStr = parts[4].replace(',', '.');//reemplaza comas por puntos en caso de que el formato use comas
				} else if (parts.length >= 6) {
					// new format: numero|titular|idCliente|tipo|pin|saldo
					pin = parts[4];
					saldoStr = parts[5].replace(',', '.');
				}
				double saldo = 0.0;
				try { saldo = Double.parseDouble(saldoStr); } catch (NumberFormatException e) { }//si el saldo no es un numero valido, queda en 0.0
				// buscar cliente por id
				Cliente titular = buscarClientePorId(idCliente);
				if (titular == null) {
					// crear cliente placeholder si no existe
					titular = new Cliente(idCliente, nombreTitular, "", java.time.LocalDate.of(1970,1,1));//fecha por defecto
					this.clientes.add(titular);//agregar el cliente placeholder a la lista de clientes
				}
				// para reconstruir la cuenta necesitamos un PIN; usar 0000 por defecto si no hay otro medio
				// crear y registrar la cuenta usando el PIN leído o el PIN por defecto
				Cuenta c = crearCuenta(numero, pin, tipo, titular);
				// ajustar saldo: si hay ATM disponible, usar su ruta (crea transaccion y registra correctamente)
				if (saldo > 0.0) {
					String idTx = "IMPORT_" + System.currentTimeMillis();//id unico basado en timestamp
					// si hay ATM, usar su metodo para depositar (crea transaccion y registra correctamente)
					if (this.atm != null) {//si el atm no es nulo
						try { this.atm.depositar(numero, saldo, idTx); } catch (Exception ex) { /* no bloquear */ }
					} else {
						// sin ATM, aplicar deposito directo con transaccion local
						Modelos.Transaccion t = new Modelos.Transaccion(Enums.TipoTransaccion.DEPOSITO, saldo, numero, idTx);
						try { c.aplicarDeposito(saldo, t); } catch (Exception ex) { }// no bloquear, si falla queda sin saldo
					}
				}
				try { long v = Long.parseLong(numero); if (v > maxCuentaId) maxCuentaId = v; } catch (NumberFormatException ex) { }//si el numero de cuenta no es un numero valido, se ignora
				// actualizar nextCuentaId despues de procesar todas las cuentas
			} catch (Exception ex) {
				// ignorar linea mal formada
			}
		}
		this.nextCuentaId = Math.max(this.nextCuentaId, maxCuentaId + 1);

		// Cargar transacciones y asociarlas a cuentas/ATM
		java.util.List<String> txLines = Reportes.leerTodasLineasTransacciones();
		for (String line : txLines) {
			try {
				// formato: idTransaccion|numeroCuenta|tipo|monto|fechaHora
				String[] p = line.split("\\|");
				if (p.length < 5) continue;
				String idTx = p[0];
				String numCuenta = p[1];
				Enums.TipoTransaccion tipo = Enums.TipoTransaccion.valueOf(p[2]);
				double monto = Double.parseDouble(p[3].replace(',', '.'));
				java.time.LocalDateTime fecha = java.time.LocalDateTime.parse(p[4]);
				Modelos.Transaccion t = new Modelos.Transaccion(tipo, monto, numCuenta, idTx, fecha);
				// agregar al ATM y a la cuenta si existe
				if (this.atm != null) this.atm.agregarTransaccion(t);
				Cuenta cu = null;
				for (Cuenta cc : this.cuentas) if (cc.getNumeroCuenta().equals(numCuenta)) { cu = cc; break; }
				if (cu != null) {
					try { cu.agregarTransaccion(t); } catch (Exception ex) { }
				}
			} catch (Exception ex) { /* ignorar lineas mal formadas */ }
		}
	}

	public List<Cliente> getClientes() { return clientes; }//aqui se obtiene la lista de clientes
	public List<Cuenta> getCuentas() { return cuentas; }// aqui se obtiene la lista de cuentas

	/** Crea y registra un cliente en memoria */
	public Cliente crearCliente(String idCliente, String nombreCompleto, String telefono, java.time.LocalDate fechaNacimiento)
	{
		Cliente c = new Cliente(idCliente, nombreCompleto, telefono, fechaNacimiento);//aqui se crea el cliente, se envia a el constructor de Cliente
		clientes.add(c);
	try { Reportes.guardarTodosClientes(this.clientes); } catch (Exception ex) { }// no bloquear si falla el guardado, mas bien se intenta nuevamente en el futuro cuando se cree otro cliente
		return c;
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
	public synchronized String generarNumeroCuenta()
	{
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
		for (Cliente c : clientes) {
			if (c.getIdCliente().equals(idCliente)) { objetivo = c; break; }//esto indica que se encontro el cliente a eliminar
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

	/** Busca un cliente por su ID (exact match) */
	public Cliente buscarClientePorId(String idCliente) {
		if (idCliente == null) return null;
		for (Cliente c : clientes) {
			if (c.getIdCliente().equals(idCliente)) return c;
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
		// actualizar archivo de control
		try { Reportes.guardarTodasCuentas(this.cuentas); } catch (Exception ex) { }
		return true;
	}
}
