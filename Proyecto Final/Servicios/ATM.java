package Servicios;

import java.util.ArrayList;
import java.util.List;

import Modelos.Transaccion;

public class ATM 
{
    private List<Transaccion> listaTransacciones;

    public ATM()
    {
        this.listaTransacciones = new ArrayList<>();
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
