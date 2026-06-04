/*
   objetivo de este archivo:
   representar los numeros clave del negocio para los paneles de control.
*/
package com.dmari.modelo;

public class DashboardEstadistica {
    // suma total de dinero por ventas brutas
    private double totalIngresos;
    // conteo de ordenes procesadas exitosamente
    private int cantidadPedidos;
    // suma de unidades fisicas entregadas
    private int totalProductosVendidos;
    private double totalComisiones; // nuevo campo para rastrear la ganancia de la empresa

    // inicializador vacio para reportes
    public DashboardEstadistica() {}

    // getters y setters siguiendo el estilo del proyecto
    public double getTotalIngresos() {
        return totalIngresos;
    }

    public void setTotalIngresos(double totalIngresos) {
        this.totalIngresos = totalIngresos;
    }

    public int getCantidadPedidos() {
        return cantidadPedidos;
    }

    public void setCantidadPedidos(int cantidadPedidos) {
        this.cantidadPedidos = cantidadPedidos;
    }

    public int getTotalProductosVendidos() {
        return totalProductosVendidos;
    }

    public void setTotalProductosVendidos(int totalProductosVendidos) {
        this.totalProductosVendidos = totalProductosVendidos;
    }

    public double getTotalComisiones() {
        return totalComisiones;
    }

    public void setTotalComisiones(double totalComisiones) {
        this.totalComisiones = totalComisiones;
    }
}