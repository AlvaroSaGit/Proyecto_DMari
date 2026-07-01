package com.dmari.dao;

import com.dmari.helper.databaseHelper;
import com.dmari.modelo.DashboardEstadistica;
import java.sql.*;

// dao para las consultas estadisticas del panel de control de dmari.
// separa la visibilidad de los datos financieros segun el rol del usuario:
// el administrador ve los numeros de toda la plataforma, mientras que el
// proveedor solo ve las cifras generadas por sus propios productos.
public class DashboardDAO {
    // instancia para la conexion a la base de datos
    private databaseHelper db = new databaseHelper();

    // obtiene las estadisticas globales de toda la plataforma (solo para administrador).
    // calcula el total de ingresos, la cantidad de pedidos entregados,
    // el total de unidades vendidas y las comisiones acumuladas de dmari,
    // filtrando unicamente los pedidos con estado entregado.
    public DashboardEstadistica obtenerEstadisticasGlobales() {
        // objeto para almacenar los resultados estadisticos
        DashboardEstadistica est = new DashboardEstadistica();
        // consulta sql para obtener totales de ingresos, pedidos, productos y comisiones
        String sql = "SELECT SUM(dp.subtotal) as ingresos, COUNT(DISTINCT p.id_pedido_pk) as pedidos, " +
                "SUM(dp.cantidad) as productos, " +
                "SUM(pg.comision_dmari) as comisiones " +
                "FROM pedido p " +
                "INNER JOIN detalle_pedido dp ON p.id_pedido_pk = dp.id_pedido_fk " +
                "INNER JOIN pago pg ON p.id_pedido_pk = pg.id_pedido_fk " +
                "WHERE p.estado_pedido = 'Entregado'";

        // ejecuta la consulta en un bloque try con recursos
        try (Connection con = db.conectar();
                PreparedStatement pst = con.prepareStatement(sql);
                ResultSet rs = pst.executeQuery()) {

            // si hay resultados, asigna los valores al objeto
            if (rs.next()) {
                est.setTotalIngresos(rs.getDouble("ingresos"));
                est.setCantidadPedidos(rs.getInt("pedidos"));
                est.setTotalProductosVendidos(rs.getInt("productos"));
                est.setTotalComisiones(rs.getDouble("comisiones"));
            }
        // captura y muestra errores de sql
        } catch (SQLException e) {
            System.err.println("error en dashboarddao global: " + e.getMessage());
        }
        // retorna el objeto con las estadisticas globales
        return est;
    }

    // obtiene las estadisticas de ventas filtradas para un proveedor especifico.
    // usa un join con proveedor_producto para garantizar que solo se
    // contabilizan los pedidos de los productos que pertenecen a ese proveedor,
    // filtrando adicionalmente por estado entregado para realismo financiero.
    public DashboardEstadistica obtenerEstadisticasPorProveedor(int idProveedor) {
        // objeto para almacenar resultados por proveedor
        DashboardEstadistica est = new DashboardEstadistica();

        // explicacion del sql:
        // 1. unimos pedido con detalle_pedido para ver que se vendio.
        // 2. unimos con proveedor_producto para filtrar solo lo que pertenece al id del proveedor.
        // 3. solo contamos pedidos en estado entregado para realismo financiero.
        String sql = "SELECT SUM(dp.subtotal) as ingresos, " +
                "COUNT(DISTINCT p.id_pedido_pk) as pedidos, " +
                "SUM(dp.cantidad) as productos " +
                "FROM pedido p " +
                "INNER JOIN detalle_pedido dp ON p.id_pedido_pk = dp.id_pedido_fk " +
                "INNER JOIN proveedor_producto pp ON dp.id_producto_fk = pp.id_producto_fk " +
                "WHERE pp.id_proveedor_fk = ? AND p.estado_pedido = 'Entregado'";

        // ejecuta la consulta
        try (Connection con = db.conectar();
                PreparedStatement pst = con.prepareStatement(sql)) {

            // asigna el parametro del id del proveedor
            pst.setInt(1, idProveedor);

            try (ResultSet rs = pst.executeQuery()) {
                // asigna los resultados si existen
                if (rs.next()) {
                    est.setTotalIngresos(rs.getDouble("ingresos"));
                    est.setCantidadPedidos(rs.getInt("pedidos"));
                    est.setTotalProductosVendidos(rs.getInt("productos"));
                }
            }
        // captura errores en la consulta
        } catch (SQLException e) {
            System.err.println("error en dashboarddao proveedor: " + e.getMessage());
        }
        // retorna las estadisticas del proveedor
        return est;
    }
}