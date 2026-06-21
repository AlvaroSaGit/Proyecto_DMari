/*
    objetivo de este archivo:
    Data Access Object (DAO) para gestionar el carrito de compras del cliente.
    Interactúa de manera directa con las tablas 'carrito' y 'detalle_carrito'.
*/
package com.dmari.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.dmari.helper.databaseHelper;
import com.dmari.modelo.detalleCarrito;

public class carritoDAO {
    
    databaseHelper db = new databaseHelper();

    /**
     * 1. obtener o crear carrito
     * este metodo actua como un portero. cuando un cliente intenta guardar productos, 
     * primero verifica si ya tiene un carrito activo en la base de datos.
     * si no tiene uno, inserta un registro nuevo con estado activo.
     * 
     * @param idcliente int: el id unico del usuario logueado.
     * @return int: el id del carrito activo, o -1 si ocurrio un error.
     */
    public int obtenerOCrearCarrito(int idCliente) {
        int idCarrito = -1;
        // consulta para buscar si el cliente ya tiene una canasta activa
        // busca un registro en la tabla carrito filtrando por el dueno y que no este procesado
        String sqlSelect = "SELECT id_carrito_pk FROM carrito WHERE id_cliente_fk = ? AND estado = 'Activo'";
        // consulta para crearle una nueva canasta con estado activo por defecto
        // inserta un nuevo encabezado de carrito vinculandolo al cliente
        String sqlInsert = "INSERT INTO carrito (id_cliente_fk, estado) VALUES (?, 'Activo')";
        // salvavidas: asegura que el cliente exista en su tabla para evitar errores de integridad
        // usa on duplicate key update para evitar el error de clave primaria duplicada si ya existía
        String sqlAsegurarCliente = "INSERT INTO cliente (id_cliente_pk) VALUES (?) ON DUPLICATE KEY UPDATE id_cliente_pk = id_cliente_pk";

        try (Connection con = db.conectar()) {
            // paso a: intentamos leer el carrito activo
            try (PreparedStatement psSelect = con.prepareStatement(sqlSelect)) {
                // buscamos por el id del usuario logueado
                psSelect.setInt(1, idCliente);
                try (ResultSet rs = psSelect.executeQuery()) {
                    if (rs.next()) return rs.getInt("id_carrito_pk");
                }
            }

            // ejecutamos el aseguramiento del perfil de cliente
            // paso b: aseguramos el perfil del cliente
            try (PreparedStatement psCli = con.prepareStatement(sqlAsegurarCliente)) {
                psCli.setInt(1, idCliente);
                // inserta si no existe, o actualiza si ya existe
                psCli.executeUpdate();
            }

            // paso c: creamos el carrito si no existia ninguno activo
            try (PreparedStatement psInsert = con.prepareStatement(sqlInsert, PreparedStatement.RETURN_GENERATED_KEYS)) {
                psInsert.setInt(1, idCliente);
                psInsert.executeUpdate();
                // capturamos la nueva llave generada para el carrito
                try (ResultSet rs = psInsert.getGeneratedKeys()) {
                    if (rs.next()) return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.out.println("error al obtener o crear carrito: " + e.getMessage());
        }
        return idCarrito;
    }

    /**
     * 2. obtener carrito en formato json
     * reconstruye la canasta del cliente cuando inicia sesion.
     * une el detalle con productos e imagenes.
     * 
     * @param idcliente int: el id unico del usuario.
     * @return string: texto formateado como arreglo json.
     */
    public String obtenerCarritoJSON(int idCliente) {
        StringBuilder json = new StringBuilder("[");
        // consulta con joins para reconstruir la visual del carrito
        // inner join dc une la cabecera con los items guardados
        // inner join p trae nombre y precio actual del producto
        // left join i trae la imagen principal (si el producto tiene una asignada)
        String sql = "SELECT dc.id_producto_fk, dc.cantidad, dc.seleccionado, p.precio, p.nombre_producto, p.stock, i.url_ruta " +
                     "FROM carrito c " +
                     "INNER JOIN detalle_carrito dc ON c.id_carrito_pk = dc.id_carrito_fk " +
                     "INNER JOIN producto p ON dc.id_producto_fk = p.id_producto_pk " +
                     "LEFT JOIN imagenes i ON p.id_producto_pk = i.id_producto_fk AND i.imagen_principal = 1 " +
                     "WHERE c.id_cliente_fk = ? AND c.estado = 'Activo'";
        
        try (Connection con = db.conectar(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idCliente);
            // ejecutamos el cruce de tablas para armar el json
            try (ResultSet rs = ps.executeQuery()) {
                boolean primero = true;
                while (rs.next()) {
                    // colocamos la coma solo entre los objetos del arreglo
                    if (!primero) json.append(",");
                    json.append("{")
                        .append("\"idProducto\":").append(rs.getInt("id_producto_fk")).append(",")
                        .append("\"nombre\":\"").append(rs.getString("nombre_producto")).append("\",")
                        .append("\"precio\":").append(rs.getDouble("precio")).append(",")
                        // agregamos la cantidad y el stock disponible para validaciones
                        .append("\"cantidad\":").append(rs.getInt("cantidad")).append(",")
                        // incluimos el estado de seleccion para el flujo de compra parcial
                        .append("\"seleccionado\":").append(rs.getBoolean("seleccionado")).append(",")
                        .append("\"stock\":").append(rs.getInt("stock")).append(",")
                        .append("\"imagen\":\"").append(rs.getString("url_ruta")).append("\"")
                        .append("}");
                    primero = false;
                }
            }
        } catch (SQLException e) { System.out.println("Error al obtener carrito JSON: " + e.getMessage()); }
        
        json.append("]");
        return json.toString();
    }

    /**
     * 3. sincronizar carrito (transaccional)
     * sincroniza el estado local del frontend con la base de datos.
     * borra el contenido anterior e inserta el nuevo en lote (batch).
     * 
     * @param idcliente int: identificador del usuario.
     * @param productoslocal arraylist<detallecarrito>: lista de productos a guardar.
     * @return boolean: true si la operacion fue exitosa.
     */
    public boolean sincronizarCarrito(int idCliente, ArrayList<detalleCarrito> productosLocal) {
        // Verificamos o creamos la canasta maestra
        int idCarrito = obtenerOCrearCarrito(idCliente);
        if (idCarrito == -1) return false;

        // elimina fisicamente todos los items anteriores del carrito para reescribirlos
        String sqlDelete = "DELETE FROM detalle_carrito WHERE id_carrito_fk = ?";
        // inserta el nuevo item incluyendo su estado de seleccion
        String sqlInsert = "INSERT INTO detalle_carrito (id_carrito_fk, id_producto_fk, cantidad, seleccionado) VALUES (?, ?, ?, ?)";

        Connection con = null;
        try {
            con = db.conectar();
            // Transaccion de Seguridad: Si borrar o insertar falla a la mitad, MySQL cancela todo el proceso
            
            // el proceso de limpieza selectiva ocurre aqui: al borrar todo e insertar solo lo que
            // el frontend envio (que ya viene filtrado), los productos que se pagaron desaparecen
            // de la tabla mientras que los no seleccionados se mantienen persistentes.
            
            // Esto evita que un cliente se quede con un carrito medio vacio por un error de internet.
            con.setAutoCommit(false); 
            
            // iniciamos el proceso de limpieza del detalle
            try (PreparedStatement psDelete = con.prepareStatement(sqlDelete)) {
                psDelete.setInt(1, idCarrito);
                psDelete.executeUpdate();
            }

            // evaluamos si la lista enviada desde el front contiene elementos
            // condicional: verifica que el arreglo no venga nulo ni vacio. 
            // si viene vacio, simplemente ignora la insercion (dejando el carrito limpio tras el delete anterior).
            if (productosLocal != null && !productosLocal.isEmpty()) {
                // preparamos la insercion por lotes para optimizar la red
                try (PreparedStatement psInsert = con.prepareStatement(sqlInsert)) {
                    // iteracion: recorre cada producto que envio el cliente desde javascript
                    // para inyectarlo en las instrucciones sql encoladas (batch).
                    for (detalleCarrito item : productosLocal) {
                        psInsert.setInt(1, idCarrito);
                        psInsert.setInt(2, item.getIdProductoFk());
                        psInsert.setInt(3, item.getCantidad());
                        // el error en la linea 169 ocurre si el modelo detalleCarrito no tiene isSeleccionado()
                        // se asume que el modelo ya cuenta con el atributo booleano 'seleccionado'
                        psInsert.setBoolean(4, item.isSeleccionado());
                        
                        // addBatch() encola las sentencias. En lugar de hacer 10 viajes a la BD, 
                        // enviaremos un solo paquete con las 10 instrucciones (optimiza velocidad y memoria)
                        psInsert.addBatch();
                    }
                    psInsert.executeBatch(); // Disparamos el paquete completo a MySQL
                }
            }

            // guardamos permanentemente todos los cambios en la base de datos
            // Si paso el borrado y la insercion, guardamos los cambios definitivamente
            con.commit();
            return true;
        } catch (SQLException e) {
            // Si exploto, revertimos todo al estado anterior (Rollback)
            try { if (con != null) con.rollback(); } catch (SQLException ex) {}
            System.out.println("Error al sincronizar carrito: " + e.getMessage());
            return false;
        } finally {
            // Obligatorio: devolver el autocommit a 'true' para no dañar futuras conexiones en el pool
            try { if (con != null) { con.setAutoCommit(true); con.close(); } } catch (SQLException e) {}
        }
    }

    /**
     * 4. vaciar carrito
     * se ejecuta tras un checkout exitoso.
     * 
     * @param idcliente int: identificador del comprador.
     * @return boolean: true si limpio la tabla.
     */
    public boolean vaciarCarrito(int idCliente) {
        // Enviamos 'null' como lista de productos. 
        // Esto hara que sincronizarCarrito ejecute el DELETE, 
        // pero salte el INSERT, dejando la tabla limpia y lista para otra compra.
        return sincronizarCarrito(idCliente, null);
    }

    /**
     * 5. actualizar seleccion individual
     * permite marcar o desmarcar un producto para la compra sin afectar al resto.
     */
    public boolean actualizarSeleccion(int idCliente, int idProducto, boolean seleccionado) {
        String sql = "UPDATE detalle_carrito dc " +
                     "JOIN carrito c ON dc.id_carrito_fk = c.id_carrito_pk " +
                     "SET dc.seleccionado = ? " +
                     "WHERE c.id_cliente_fk = ? AND dc.id_producto_fk = ? AND c.estado = 'Activo'";
        try (Connection con = db.conectar(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setBoolean(1, seleccionado);
            ps.setInt(2, idCliente);
            ps.setInt(3, idProducto);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("error al actualizar seleccion: " + e.getMessage());
            return false;
        }
    }

    /**
     * 6. limpiar items comprados
     * elimina solo los productos que fueron marcados como seleccionados.
     * se invoca despues de que productos_confirmados haya capturado los datos.
     */
    public void limpiarItemsComprados(int idCarrito) {
        String sql = "DELETE FROM detalle_carrito WHERE id_carrito_fk = ? AND seleccionado = true";
        try (Connection con = db.conectar(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idCarrito);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("error al limpiar items comprados: " + e.getMessage());
        }
    }
}