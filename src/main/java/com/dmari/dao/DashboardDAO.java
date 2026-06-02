/*
   objetivo de este archivo:
   consultar la base de datos para generar calculos financieros.
   separa la visibilidad de dinero segun el rol del usuario.
*/
package com.dmari.dao;

import com.dmari.helper.databaseHelper;
import com.dmari.modelo.DashboardEstadistica;
import java.sql.*;

public class DashboardDAO {
    private databaseHelper db = new databaseHelper();

    // el administrador ve los ingresos globales de toda la plataforma
    public DashboardEstadistica obtenerEstadisticasGlobales() {
        DashboardEstadistica est = new DashboardEstadistica();
        // sumamos los ingresos brutos y las comisiones reales cobradas en la tabla pago
        String sql = "SELECT SUM(p.total_pagar) as ingresos, COUNT(p.id_pedido_pk) as pedidos, " +
                     "(SELECT SUM(cantidad) FROM detalle_pedido) as productos, " +
                     "SUM(pg.comision_dmari) as comisiones " +
                     "FROM pedido p INNER JOIN pago pg ON p.id_pedido_pk = pg.id_pedido_fk " +
                     "WHERE p.estado_pedido = 'Entregado'";

        try (Connection con = db.conectar();
             PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            
            if (rs.next()) {
                est.setTotalIngresos(rs.getDouble("ingresos"));
                est.setCantidadPedidos(rs.getInt("pedidos"));
                est.setTotalProductosVendidos(rs.getInt("productos"));
                est.setTotalComisiones(rs.getDouble("comisiones"));
            }
        } catch (SQLException e) {
            System.err.println("error en dashboarddao global: " + e.getMessage());
        }
        return est;
    }

    // el proveedor solo ve el dinero generado por sus propios productos
    public DashboardEstadistica obtenerEstadisticasPorProveedor(int idProveedor) {
        DashboardEstadistica est = new DashboardEstadistica();
        
        /* 
           explicacion del sql:
           1. unimos pedido con detalle_pedido para ver que se vendio.
           2. unimos con proveedor_producto para filtrar solo lo que pertenece al id del proveedor.
           3. solo contamos pedidos en estado entregado para realismo financiero.
        */
        String sql = "SELECT SUM(dp.subtotal) as ingresos, " +
                     "COUNT(DISTINCT p.id_pedido_pk) as pedidos, " +
                     "SUM(dp.cantidad) as productos " +
                     "FROM pedido p " +
                     "INNER JOIN detalle_pedido dp ON p.id_pedido_pk = dp.id_pedido_fk " +
                     "INNER JOIN proveedor_producto pp ON dp.id_producto_fk = pp.id_producto_fk " +
                     "WHERE pp.id_proveedor_fk = ? AND p.estado_pedido = 'Entregado'";

        try (Connection con = db.conectar();
             PreparedStatement pst = con.prepareStatement(sql)) {
            
            pst.setInt(1, idProveedor);
            
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    est.setTotalIngresos(rs.getDouble("ingresos"));
                    est.setCantidadPedidos(rs.getInt("pedidos"));
                    est.setTotalProductosVendidos(rs.getInt("productos"));
                }
            }
        } catch (SQLException e) {
            System.err.println("error en dashboarddao proveedor: " + e.getMessage());
        }
        return est;
    }
}