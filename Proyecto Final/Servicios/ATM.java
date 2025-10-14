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
        
    }
    
}
