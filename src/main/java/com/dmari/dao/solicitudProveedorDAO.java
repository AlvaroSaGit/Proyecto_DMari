package com.dmari.dao;

import com.dmari.helper.databaseHelper;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class solicitudProveedorDAO {
    private databaseHelper db = new databaseHelper();

    /**
     * Crea una nueva solicitud para que un usuario se convierta en proveedor.
     * @param idUsuario El ID del usuario que realiza la solicitud.
     * @param nit El NIT de la empresa.
     * @param marca El nombre de la marca.
     * @param cuenta El número de cuenta bancaria.
     * @param banco El nombre del banco.
     * @param tipoCuenta El tipo de cuenta (Ahorros/Corriente).
     * @return true si la solicitud se creó con éxito.
     */
    public boolean crearSolicitud(int idUsuario, String nit, String marca, String cuenta, String banco, String tipoCuenta) {
        String sql = "INSERT INTO solicitud_proveedor (id_usuario_fk, nit_empresa, nombre_marca, cuenta_bancaria, banco_nombre, tipo_cuenta, estado_solicitud) VALUES (?, ?, ?, ?, ?, ?, 'pendiente')";
        try (Connection con = db.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.setString(2, nit);
            ps.setString(3, marca);
            ps.setString(4, cuenta);
            ps.setString(5, banco);
            ps.setString(6, tipoCuenta);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al crear solicitud de proveedor: " + e.getMessage());
            return false;
        }
    }
}