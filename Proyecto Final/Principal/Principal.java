package Principal;

import java.time.LocalDate;

import Enums.TipoCuenta;
import Modelos.Cliente;
import Modelos.Cuenta;

public class Principal 
{
   public static void main(String[] args) 
   {
    Cliente cliente = new Cliente("12345678", "Juan Pérez", "555-1234", LocalDate.of(1990, 5, 15));
    System.out.println(cliente.mostrarDetallesCliente());
   

    TipoCuenta tipoCuenta = Utilitaria.ScannerUtil.capturarTipoCuenta("Ingrese el tipo de cuenta");
    Cuenta cuenta = new Cuenta("000123456789", "1234", tipoCuenta, cliente);
    System.out.println(cuenta.mostrarDetallesCuenta());


    
   }     
}
