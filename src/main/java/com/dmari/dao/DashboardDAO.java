package com.dmari.dao;

import com.dmari.helper.databaseHelper;
import com.dmari.modelo.DashboardEstadistica;
import java.sql.*;

/**
 * DAO para las consultas estadisticas del panel de control (dashboard) de
 * DMari.
 *
 * Separa la visibilidad de los datos financieros segun el rol del usuario:
 * el Administrador ve los numeros de toda la plataforma, mientras que el
 * Proveedor solo ve las cifras generadas por sus propios productos.
 *
 */
public class DashboardDAO {
    private databaseHelper db = new databaseHelper();

    /**
     * Obtiene las estadisticas globales de toda la plataforma (solo para
     * Administrador).
     *
     * Calcula el total de ingresos, la cantidad de pedidos entregados,
     * el total de unidades vendidas y las comisiones acumuladas de DMari,
     * filtrando unicamente los pedidos con estado Entregado.
     *
     * @return #DashboardEstadistica con los datos agregados de la plataforma.
     *         Los campos seran cero si no hay pedidos entregados registrados.
     */
    public DashboardEstadistica obtenerEstadisticasGlobales() {
        DashboardEstadistica est = new DashboardEstadistica();
        String sql = "SELECT SUM(dp.subtotal) as ingresos, COUNT(DISTINCT p.id_pedido_pk) as pedidos, " +
                "SUM(dp.cantidad) as productos, " +
                "SUM(pg.comision_dmari) as comisiones " +
                "FROM pedido p " +
                "INNER JOIN detalle_pedido dp ON p.id_pedido_pk = dp.id_pedido_fk " +
                "INNER JOIN pago pg ON p.id_pedido_pk = pg.id_pedido_fk " +
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

    /**
     * Obtiene las estadisticas de ventas filtradas para un proveedor especifico.
     *
     * Usa un JOIN con #proveedor_producto para garantizar que solo se
     * contabilizan los pedidos de los productos que pertenecen a ese proveedor,
     * filtrando adicionalmente por estado 'Entregado' para realismo financiero.
     *
     * @param idProveedor int con el ID del proveedor en sesion.
     * @return #DashboardEstadistica con los ingresos, pedidos y unidades
     *         vendidas exclusivamente de ese proveedor. Los campos seran cero si
     *         no tiene ventas entregadas.
     */
    public DashboardEstadistica obtenerEstadisticasPorProveedor(int idProveedor) {
        DashboardEstadistica est = new DashboardEstadistica();

        /*
         * explicacion del sql:
         * 1. unimos pedido con detalle_pedido para ver que se vendio.
         * 2. unimos con proveedor_producto para filtrar solo lo que pertenece al id del
         * proveedor.
         * 3. solo contamos pedidos en estado entregado para realismo financiero.
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