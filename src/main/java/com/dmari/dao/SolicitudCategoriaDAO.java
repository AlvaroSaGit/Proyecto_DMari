/*
   objetivo de este archivo:
   gestionar las peticiones de nuevas categorias que envian los proveedores.
   permite listar, insertar y aprobar solicitudes mediante procesos transaccionales.
*/
package com.dmari.dao;

import com.dmari.helper.databaseHelper;
import com.dmari.modelo.SolicitudCategoria;
import java.sql.*;
import java.util.ArrayList;

public class SolicitudCategoriaDAO {
    private databaseHelper db = new databaseHelper();

    // obtiene todas las solicitudes registradas haciendo un cruce con la tabla usuario para saber quien la hizo
    public ArrayList<SolicitudCategoria> listarTodas() {
        ArrayList<SolicitudCategoria> lista = new ArrayList<>();
        String sql = "SELECT s.*, u.nombre as nombre_proveedor FROM solicitud_categoria s " +
                     "INNER JOIN usuario u ON s.id_proveedor_fk = u.id_usuario_pk " +
                     "ORDER BY s.fecha_creacion DESC";
        
        // usamos el bloque try con recursos para asegurar que la conexion se cierre al terminar
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

    // guarda una nueva sugerencia de categoria en la base de datos
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

    // cambia el estado de una solicitud (ej. de pendiente a rechazada)
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
    
    // metodo transaccional: marca la solicitud como aprobada e inserta la categoria real al mismo tiempo
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
            
            // paso 2: crear la categoria formal en la tabla de categorias para que aparezca en la tienda
            String sqlInsert = "INSERT INTO categoria (nombre, descripcion, estado_activo) VALUES (?, 'Categoria sugerida por proveedor', 1)";
            PreparedStatement pstInsert = con.prepareStatement(sqlInsert);
            pstInsert.setString(1, nombreCat);
            pstInsert.executeUpdate();
            
            con.commit();
            return true;
            // si algo falla en cualquiera de los dos pasos, deshacemos todo para evitar datos huerfanos
        } catch (SQLException e) {
            try { if (con != null) con.rollback(); } catch (SQLException ex) {}
            return false;
        } finally { db.cerrar(con); }
    }
}
