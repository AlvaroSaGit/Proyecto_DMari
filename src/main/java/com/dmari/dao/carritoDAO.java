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
     * 1. Obtener o Crear Carrito
     * Este metodo actua como un "portero". Cuando un cliente intenta guardar productos, 
     * primero verifica si ya tiene un carrito asignado en la base de datos.
     * Si no tiene uno (porque es su primera vez), inserta un registro nuevo y le asigna un ID.
     * 
     * @param idCliente int: el id unico del usuario logueado.
     * @return int: el id (llave primaria) del carrito de ese cliente, o -1 si ocurrio un error en sql.
     */
    public int obtenerOCrearCarrito(int idCliente) {
        int idCarrito = -1;
        // Consulta para buscar si el cliente ya tiene una canasta asignada
        String sqlSelect = "SELECT id_carrito_pk FROM carrito WHERE id_cliente_fk = ?";
        // Consulta para crearle una nueva canasta si la busqueda anterior no arroja resultados
        String sqlInsert = "INSERT INTO carrito (id_cliente_fk) VALUES (?)";
        // salvavidas: asegura que el cliente exista en su tabla hija para que la llave foranea no explote
        String sqlAsegurarCliente = "INSERT INTO cliente (id_cliente_pk, direccion_envio) VALUES (?, 'sin registrar') ON DUPLICATE KEY UPDATE direccion_envio = direccion_envio";

        try (Connection con = db.conectar()) {
            // PASO A: Intentamos leer la base de datos
            try (PreparedStatement psSelect = con.prepareStatement(sqlSelect)) {
                psSelect.setInt(1, idCliente);
                try (ResultSet rs = psSelect.executeQuery()) {
                    // condicional: evalua si el cursor de la base de datos encontro al menos un resultado.
                    // si es true, extraemos el id existente y cortamos la funcion retornandolo.
                    if (rs.next()) {
                        return rs.getInt("id_carrito_pk");
                    }
                }
            }

            // inyectamos un perfil basico fantasma antes de intentar crear el carrito.
            // usamos 'insert ignore' para que, si el usuario ya habia llenado su perfil, mysql simplemente ignore la instruccion.
            try (PreparedStatement psCli = con.prepareStatement(sqlAsegurarCliente)) {
                psCli.setInt(1, idCliente);
                psCli.executeUpdate();
            }

            // PASO B: Si llegamos a esta linea, significa que el cliente NO tenia carrito.
            // Preparamos el INSERT pidiendole a MySQL que nos devuelva la llave primaria que genere (RETURN_GENERATED_KEYS)
            try (PreparedStatement psInsert = con.prepareStatement(sqlInsert, PreparedStatement.RETURN_GENERATED_KEYS)) {
                psInsert.setInt(1, idCliente);
                psInsert.executeUpdate();
                // Atrapamos el ID nuevo que MySQL acaba de crear
                try (ResultSet rs = psInsert.getGeneratedKeys()) {
                    // condicional: verifica si mysql nos devolvio exitosamente la llave autogenerada.
                    // si es asi, la retornamos para que el sistema sepa cual es su nuevo carrito.
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener o crear carrito: " + e.getMessage());
        }
        return idCarrito;
    }

    /**
     * 2. Obtener Carrito en formato JSON
     * Reconstruye visualmente la canasta del cliente cuando inicia sesion.
     * Hace un cruce de multiples tablas (JOINs) para juntar el ID del producto, 
     * su cantidad, su nombre, su precio y su foto principal en una sola cadena de texto.
     * 
     * @param idCliente int: el id unico del usuario.
     * @return string: un texto formateado como un arreglo de objetos json (ej: "[{...}]").
     */
    public String obtenerCarritoJSON(int idCliente) {
        // Usamos StringBuilder porque es mucho mas rapido y eficiente en memoria que usar String normal ("" + "")
        StringBuilder json = new StringBuilder("[");
        String sql = "SELECT dc.id_producto_fk, dc.cantidad, p.precio, p.nombre_producto, i.url_ruta " +
                     "FROM carrito c " +
                     "INNER JOIN detalle_carrito dc ON c.id_carrito_pk = dc.id_carrito_fk " + // Une la canasta con sus productos
                     "INNER JOIN producto p ON dc.id_producto_fk = p.id_producto_pk " + // Trae el nombre y precio
                     "LEFT JOIN imagenes i ON p.id_producto_pk = i.id_producto_fk AND i.imagen_principal = 1 " + // Trae la foto (si tiene)
                     "WHERE c.id_cliente_fk = ?";
        
        try (Connection con = db.conectar(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idCliente);
            try (ResultSet rs = ps.executeQuery()) {
                boolean primero = true;
                // iteracion: recorre fila por fila todos los productos que el cliente tiene en el carrito.
                // por cada vuelta, construye un trozo de texto con formato json.
                while (rs.next()) {
                    // condicional: si no es el primer elemento del ciclo, insertamos una coma 
                    // para separar los objetos json correctamente (ej: {...},{...}).
                    if (!primero) json.append(",");
                    json.append("{")
                        .append("\"idProducto\":").append(rs.getInt("id_producto_fk")).append(",")
                        .append("\"nombre\":\"").append(rs.getString("nombre_producto")).append("\",")
                        .append("\"precio\":").append(rs.getDouble("precio")).append(",")
                        .append("\"cantidad\":").append(rs.getInt("cantidad")).append(",")
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
     * 3. Sincronizar Carrito (Transaccional y por Lotes)
     * Este es el metodo mas importante de esta clase. Toma lo que el cliente tiene 
     * en su pantalla (Frontend) y obliga a la base de datos a ser una copia identica.
     * Para evitar errores, borra todo lo viejo y guarda lo nuevo en un solo bloque.
     * 
     * @param idCliente int: identificador del usuario.
     * @param productosLocal arraylist<detallecarrito>: lista de objetos java con las cantidades e ids de los productos a guardar.
     * @return boolean: true si el borrado y la insercion multiple fueron exitosos, false si fallo el sql.
     */
    public boolean sincronizarCarrito(int idCliente, ArrayList<detalleCarrito> productosLocal) {
        // Verificamos o creamos la canasta maestra
        int idCarrito = obtenerOCrearCarrito(idCliente);
        if (idCarrito == -1) return false;

        String sqlDelete = "DELETE FROM detalle_carrito WHERE id_carrito_fk = ?";
        String sqlInsert = "INSERT INTO detalle_carrito (id_carrito_fk, id_producto_fk, cantidad) VALUES (?, ?, ?)";

        Connection con = null;
        try {
            con = db.conectar();
            // Transaccion de Seguridad: Si borrar o insertar falla a la mitad, MySQL cancela todo el proceso
            // Esto evita que un cliente se quede con un carrito medio vacio por un error de internet.
            con.setAutoCommit(false); 

            // PASO 1: Destruimos todos los productos que estaban guardados anteriormente en la BD
            try (PreparedStatement psDelete = con.prepareStatement(sqlDelete)) {
                psDelete.setInt(1, idCarrito);
                psDelete.executeUpdate();
            }

            // condicional: verifica que el arreglo no venga nulo ni vacio. 
            // si viene vacio, simplemente ignora la insercion (dejando el carrito limpio tras el delete anterior).
            if (productosLocal != null && !productosLocal.isEmpty()) {
                try (PreparedStatement psInsert = con.prepareStatement(sqlInsert)) {
                    // iteracion: recorre cada producto que envio el cliente desde javascript
                    // para inyectarlo en las instrucciones sql encoladas (batch).
                    for (detalleCarrito item : productosLocal) {
                        psInsert.setInt(1, idCarrito);
                        psInsert.setInt(2, item.getIdProductoFk());
                        psInsert.setInt(3, item.getCantidad());
                        
                        // addBatch() encola las sentencias. En lugar de hacer 10 viajes a la BD, 
                        // enviaremos un solo paquete con las 10 instrucciones (optimiza velocidad y memoria)
                        psInsert.addBatch();
                    }
                    psInsert.executeBatch(); // Disparamos el paquete completo a MySQL
                }
            }

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
     * 4. Vaciar carrito
     * Metodo auxiliar diseñado para ejecutarse justo despues de que un pedido 
     * se convierte en una venta real (checkout exitoso).
     * 
     * @param idCliente int: identificador unico del comprador.
     * @return boolean: true si logro limpiar la tabla, false si fallo.
     */
    public boolean vaciarCarrito(int idCliente) {
        // Enviamos 'null' como lista de productos. 
        // Esto hara que sincronizarCarrito ejecute el DELETE, 
        // pero salte el INSERT, dejando la tabla limpia y lista para otra compra.
        return sincronizarCarrito(idCliente, null);
    }
}