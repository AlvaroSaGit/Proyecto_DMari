/*
    objetivo de este archivo:
    gestiona la extraccion de etiquetas desde la base de datos.
    permite traer todas las etiquetas globales o cruzar tablas (joins)
    para encontrar las etiquetas que pertenecen a un producto especifico.
*/
package com.dmari.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.dmari.helper.databaseHelper;
import com.dmari.modelo.etiqueta;

public class etiquetaDAO {
    
    databaseHelper db = new databaseHelper();

    // Metodo para listar TODAS las etiquetas (Para mostrarlas en los botones de filtro)
    public ArrayList<etiqueta> listarEtiquetas() {
        ArrayList<etiqueta> lista = new ArrayList<>();
        String sql = "SELECT id_etiqueta_pk, nombre_etiqueta FROM etiqueta";
        
        try (Connection con = db.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                etiqueta e = new etiqueta();
                e.setIdEtiquetaPk(rs.getInt("id_etiqueta_pk"));
                e.setNombreEtiqueta(rs.getString("nombre_etiqueta"));
                lista.add(e);
            }
        } catch (SQLException e) {
            System.out.println("Error al listar etiquetas: " + e.getMessage());
        }
        return lista;
    }
    
    // Metodo para obtener las etiquetas que le pertenecen a UN producto en especifico
    public ArrayList<String> listarEtiquetasPorProducto(int idProducto) {
        ArrayList<String> lista = new ArrayList<>();
        // arrancamos consultando la tabla principal de etiquetas
        String sql = "SELECT e.nombre_etiqueta FROM etiqueta e " +
                     // uso de inner join (cruce estricto): 
                     // obliga a que la etiqueta exista fisicamente en la tabla puente apuntando al producto solicitado.
                     // si la etiqueta existe en el sistema pero no esta amarrada al producto, se descarta.
                     "INNER JOIN producto_etiqueta pe ON e.id_etiqueta_pk = pe.id_etiqueta " +
                     "WHERE pe.id_producto = ?";
                     
        try (Connection con = db.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
             
            ps.setInt(1, idProducto);
            try(ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(rs.getString("nombre_etiqueta"));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al listar etiquetas del producto: " + e.getMessage());
        }
        return lista;
    }

    // metodo que procesa el texto separado por comas, crea etiquetas nuevas si no existen y las vincula al producto
    public void actualizarEtiquetasDeProducto(int idProducto, String etiquetasStr) {
        // 1. borramos las relaciones viejas para que no queden duplicados al editar
        String sqlDelete = "DELETE FROM producto_etiqueta WHERE id_producto = ?";
        try (Connection con = db.conectar(); PreparedStatement psDel = con.prepareStatement(sqlDelete)) {
            psDel.setInt(1, idProducto);
            psDel.executeUpdate();
        } catch (SQLException e) { System.out.println("error al limpiar etiquetas viejas: " + e.getMessage()); }

        // si el usuario no escribio nada, terminamos el proceso aqui
        if (etiquetasStr == null || etiquetasStr.trim().isEmpty()) return;

        // 2. separamos el texto por las comas
        String[] tags = etiquetasStr.split(",");
        String sqlBuscar = "SELECT id_etiqueta_pk FROM etiqueta WHERE nombre_etiqueta = ?";
        String sqlInsertarTag = "INSERT INTO etiqueta (nombre_etiqueta) VALUES (?)";
        String sqlVincular = "INSERT INTO producto_etiqueta (id_producto, id_etiqueta) VALUES (?, ?)";

        try (Connection con = db.conectar()) {
            for (String tag : tags) {
                tag = tag.trim(); // quitamos espacios en blanco accidentales
                if (tag.isEmpty()) continue;

                int idEtiqueta = 0;
                
                // paso a: buscar si la etiqueta ya existe en el sistema
                try (PreparedStatement psBuscar = con.prepareStatement(sqlBuscar)) {
                    psBuscar.setString(1, tag);
                    try (ResultSet rs = psBuscar.executeQuery()) {
                        if (rs.next()) idEtiqueta = rs.getInt("id_etiqueta_pk");
                    }
                }

                // paso b: si no existe (id 0), la insertamos como una etiqueta nueva
                if (idEtiqueta == 0) {
                    try (PreparedStatement psInsert = con.prepareStatement(sqlInsertarTag, PreparedStatement.RETURN_GENERATED_KEYS)) {
                        psInsert.setString(1, tag);
                        psInsert.executeUpdate();
                        try (ResultSet rsKeys = psInsert.getGeneratedKeys()) {
                            if (rsKeys.next()) idEtiqueta = rsKeys.getInt(1);
                        }
                    }
                }

                // paso c: vinculamos la etiqueta (nueva o vieja) con nuestro producto
                if (idEtiqueta > 0) {
                    try (PreparedStatement psVincular = con.prepareStatement(sqlVincular)) {
                        psVincular.setInt(1, idProducto);
                        psVincular.setInt(2, idEtiqueta);
                        psVincular.executeUpdate();
                    }
                }
            }
            
            // 3. LIMPIEZA DE BASURA (Garbage Collection): Borrar etiquetas huerfanas
            // Si una etiqueta ya no esta vinculada a ningun producto, la eliminamos para no dejar basura en la base de datos.
            String sqlLimpiar = "DELETE FROM etiqueta WHERE id_etiqueta_pk NOT IN (SELECT DISTINCT id_etiqueta FROM producto_etiqueta)";
            try (PreparedStatement psLimpiar = con.prepareStatement(sqlLimpiar)) {
                psLimpiar.executeUpdate();
            }
        } catch (SQLException e) { System.out.println("error al procesar etiquetas: " + e.getMessage()); }
    }
}