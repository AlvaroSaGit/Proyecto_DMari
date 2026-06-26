package com.dmari.dao;

import com.dmari.helper.databaseHelper;
import com.dmari.modelo.SolicitudCategoria;
import java.sql.*;
import java.util.ArrayList;

/**
 * DAO para la gestion de solicitudes de nuevas categorias en DMari.
 *
 * 
 * Gestiona el flujo completo de peticiones que los proveedores envian
 * al administrador cuando desean que se cree una nueva categoria de productos.
 * 
 *
 * 
 * El metodo #aprobarSolicitud(int, String) es transaccional:
 * actualiza el estado de la solicitud E inserta la nueva categoria en la
 * tabla categoria de forma atomica (todo o nada).
 *
 */
public class SolicitudCategoriaDAO {
    private databaseHelper db = new databaseHelper();

    /**
     * Obtiene todas las solicitudes registradas, incluyendo el nombre del proveedor
     * que las hizo a traves de un INNER JOIN con la tabla usuario.
     *
     * @return #java.util.ArrayList con todos los objetos
     *         #SolicitudCategoria
     *         ordenados por fecha de creacion descendente (las mas recientes
     *         primero).
     *         Devuelve una lista vacia si no hay solicitudes o si ocurre un error
     *         SQL.
     */
    public ArrayList<SolicitudCategoria> listarTodas() {
        ArrayList<SolicitudCategoria> lista = new ArrayList<>();
        String sql = "SELECT s.*, u.nombre as nombre_proveedor FROM solicitud_categoria s " +
                "INNER JOIN usuario u ON s.id_proveedor_fk = u.id_usuario_pk " +
                "ORDER BY s.fecha_creacion DESC";

        // usamos el bloque try con recursos para asegurar que la conexion se cierre al
        // terminar
        try (Connection con = db.conectar();
                PreparedStatement pst = con.prepareStatement(sql);
                ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                SolicitudCategoria sol = new SolicitudCategoria();
                sol.setIdSolicitudPk(rs.getInt("id_solicitud_pk"));
                sol.setNombreProveedor(rs.getString("nombre_proveedor"));
                sol.setNombreSugerido(rs.getString("nombre_sugerido"));
                sol.setJustificacion(rs.getString("justificacion"));
                sol.setEstadoSolicitud(rs.getString("estado_solicitud"));
                sol.setFechaCreacion(rs.getTimestamp("fecha_creacion"));
                lista.add(sol);
            }
        } catch (SQLException e) {
            System.err.println("Error en SolicitudCategoriaDAO.listarTodas: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Guarda una nueva solicitud de categoria enviada por un proveedor.
     *
     * @param idProveedor   int con el ID del usuario proveedor que realiza
     *                      la solicitud.
     * @param nombre        String con el nombre que el proveedor sugiere
     *                      para la nueva categoria.
     * @param justificacion String con la explicacion de por que se necesita
     *                      esa categoria.
     * @return boolean si la insercion fue exitosa; false si ocurrio un
     *         error SQL.
     */
    public boolean insertar(int idProveedor, String nombre, String justificacion) {
        String sql = "INSERT INTO solicitud_categoria (id_proveedor_fk, nombre_sugerido, justificacion) VALUES (?, ?, ?)";
        // inyectamos los datos del proveedor y su propuesta
        try (Connection con = db.conectar();
                PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, idProveedor);
            pst.setString(2, nombre);
            pst.setString(3, justificacion);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Cambia el estado de una solicitud existente.
     *
     * Se usa principalmente para marcar una solicitud como rechazada
     * cuando el administrador decide no aprobarla.
     *
     * @param idSolicitud int con la llave primaria de la solicitud a
     *                    modificar.
     * @param nuevoEstado String con el nuevo estado (ej.
     *                    rechazada, pendiente).
     * @return true si la actualizacion afecto al menos un registro;
     *         false si fallo.
     */
    public boolean actualizarEstado(int idSolicitud, String nuevoEstado) {
        String sql = "UPDATE solicitud_categoria SET estado_solicitud = ? WHERE id_solicitud_pk = ?";
        // localizamos la solicitud por su llave primaria para aplicar el cambio
        try (Connection con = db.conectar();
                PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, nuevoEstado);
            pst.setInt(2, idSolicitud);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Aprueba una solicitud de categoria usando una transaccion atomica de dos
     * pasos.
     *
     * Paso 1: Actualiza el estado de la solicitud a aprobada.
     * Paso 2: Inserta la nueva categoria en la tabla categoria con
     * estado activo para que aparezca en la tienda.
     *
     * Si cualquiera de los dos pasos falla, se hace ROLLBACK para evitar
     * datos huerfanos (solicitud aprobada pero sin categoria creada o viceversa).
     *
     *
     * @param idSolicitud int con la llave primaria de la solicitud a aprobar.
     * @param nombreCat   String con el nombre final que tendra la nueva categoria.
     * @return true si ambas operaciones se completaron exitosamente;
     *         false si fallo.
     */
    public boolean aprobarSolicitud(int idSolicitud, String nombreCat) {
        Connection con = null;
        try {
            con = db.conectar();
            con.setAutoCommit(false); // apagamos el autoguardado para iniciar una transaccion segura

            // paso 1: actualizar el estado de la solicitud a aprobada
            String sqlUpdate = "UPDATE solicitud_categoria SET estado_solicitud = 'aprobada' WHERE id_solicitud_pk = ?";
            PreparedStatement pstUpdate = con.prepareStatement(sqlUpdate);
            pstUpdate.setInt(1, idSolicitud);
            pstUpdate.executeUpdate();

            // paso 2: crear la categoria formal en la tabla de categorias para que aparezca
            // en la tienda
            String sqlInsert = "INSERT INTO categoria (nombre, descripcion, estado_activo) VALUES (?, 'Categoria sugerida por proveedor', 1)";
            PreparedStatement pstInsert = con.prepareStatement(sqlInsert);
            pstInsert.setString(1, nombreCat);
            pstInsert.executeUpdate();

            con.commit();
            return true;
            // si algo falla en cualquiera de los dos pasos, deshacemos todo para evitar
            // datos huerfanos
        } catch (SQLException e) {
            try {
                if (con != null)
                    con.rollback();
            } catch (SQLException ex) {
            }
            return false;
        } finally {
            db.cerrar(con);
        }
    }
}
