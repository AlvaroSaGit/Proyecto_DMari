package com.dmari.dao;

/**
 * objetivo de este archivo:
 * data access object (dao) central del catalogo.
 * orquesta complejas consultas (left joins e inner joins) para recuperar el inventario,
 * aplicando bloqueos si un producto, categoria o proveedor estan inactivos.
 */
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.dmari.helper.databaseHelper;
import com.dmari.modelo.producto;



public class productoDAO {
    databaseHelper db = new databaseHelper();
    
    /**
     * metodo de lectura global con filtros.
     * trae los productos publicos de la base de datos.
     * 
     * @param soloActivos boolean: si es true, filtra solo los que tienen stock y estado=1.
     * @return arraylist<producto>: lista de productos ensamblados.
     */
    public ArrayList<producto> listarProductos(boolean soloActivos){
        ArrayList<producto> lista = new ArrayList<>();

        String sql = "SELECT " +
             "p.id_producto_pk, " +
             "p.nombre_producto, " +
             "p.descripcion, " +
             "p.precio, " +
             "p.stock, " +
             "p.estado, " +
             "p.id_categoria_fk, " +
             // ruta principal de la foto desde la tabla unida
             "i.url_ruta, " +
             // extraemos el texto de la categoria para que el usuario no vea solo un numero
             "c.nombre AS nombre_categoria, " +
             "(SELECT GROUP_CONCAT(e.nombre_etiqueta SEPARATOR ',') FROM producto_etiqueta pe INNER JOIN etiqueta e ON pe.id_etiqueta = e.id_etiqueta_pk WHERE pe.id_producto = p.id_producto_pk) AS etiquetas_str, " +
             "prov.nombre_marca, " +
             "tel.numero_telefonico AS contacto_tel, " +
             "cor.correo AS contacto_correo " +
             "FROM producto p " +
             // uso de left join: traemos el producto incluso si no tiene imagen asignada
             "LEFT JOIN imagenes i ON p.id_producto_pk = i.id_producto_fk AND i.imagen_principal = 1 " +
             // uso de left join: traemos el nombre de la categoria. si el producto quedo sin categoria por algun error, no se ocultara.
             "LEFT JOIN categoria c ON p.id_categoria_fk = c.id_categoria_pk " +
             // nuevos left joins: traemos al proveedor (si tiene) para validar si su cuenta sigue activa
             "LEFT JOIN proveedor_producto pp ON p.id_producto_pk = pp.id_producto_fk " +
             "LEFT JOIN usuario u ON pp.id_proveedor_fk = u.id_usuario_pk " +
             "LEFT JOIN proveedor prov ON u.id_usuario_pk = prov.id_proveedor_pk " +
             "LEFT JOIN telefono tel ON u.id_usuario_pk = tel.id_usuario_fk " +
             "LEFT JOIN correo cor ON u.id_usuario_pk = cor.id_usuario_fk AND cor.correo_primario = 1";
        
        // condicional: anexa exigencias estrictas al motor de base de datos
        if (soloActivos) {
            sql += " WHERE p.estado = 1 AND p.stock > 0 AND (c.estado_activo = 1 OR c.id_categoria_pk IS NULL) AND (u.estado_cuenta = 1 OR u.id_usuario_pk IS NULL)";
        }

        try(
            Connection con = db.conectar();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()){
            
            // iteracion: transforma las filas (records) devueltas por mysql en objetos de java.
            while(rs.next()){
                producto prod = new producto();
                prod.setIdProductoPk(rs.getInt("id_producto_pk"));
                prod.setNombreProducto(rs.getString("nombre_producto"));
                prod.setDescripcion(rs.getString("descripcion"));
                prod.setPrecio(rs.getDouble("precio"));
                prod.setStock(rs.getInt("stock"));
                prod.setEstado(rs.getBoolean("estado"));
                String ruta = rs.getString("url_ruta");
                prod.setUrlRuta(ruta != null ? ruta : "src/img/productos/default/gato_programador.jpg");
                
                prod.setIdCategoriaFk(rs.getInt("id_categoria_fk"));
                String cat = rs.getString("nombre_categoria");
                prod.setCategoria(cat != null ? cat : "General");
                
                // extraemos los datos de contacto y marca del proveedor vinculados
                String marca = rs.getString("nombre_marca");
                prod.setProveedorMarca(marca != null ? marca : "DMari Oficial");
                String tel = rs.getString("contacto_tel");
                prod.setProveedorTelefono(tel != null ? tel : "N/A");
                String correo = rs.getString("contacto_correo");
                prod.setProveedorCorreo(correo != null ? correo : "contacto@dmari.com");
                
                // extraemos la cadena de multiples etiquetas y la convertimos en un arreglo (lista)
                String etiquetasStr = rs.getString("etiquetas_str");
                ArrayList<String> listaTags = new ArrayList<>();
                // condicional anidado con iteracion: deserializa las etiquetas (ej: dulce,regalo) en un array list.
                if (etiquetasStr != null && !etiquetasStr.isEmpty()) {
                    String[] tagsArray = etiquetasStr.split(",");
                    for (String t : tagsArray) {
                        listaTags.add(t);
                    }
                }
                prod.setEtiquetas(listaTags);

                lista.add(prod);
            }
        }catch(SQLException error){
            System.out.println("Problema en el dao al listar productos: "+error.getMessage());
        }
        
        return lista;
    }
    
    /**
     * aislamiento de inventario de proveedor.
     * lista el catalogo interno que le pertenece a un solo dueno (sin importar si esta pausado).
     * usa un inner join estricto como escudo de seguridad.
     * 
     * @param idUsuarioProveedor int: id de la cuenta del proveedor en cuestion.
     * @return arraylist<producto>: sus productos exclusivos.
     */
    public ArrayList<producto> listarProductosPorProveedor(int idUsuarioProveedor) {
        ArrayList<producto> lista = new ArrayList<>();
        
        String sql = "SELECT " +
             // seleccionamos la informacion basica desde la tabla producto
             "p.id_producto_pk, " +
             "p.nombre_producto, " +
             "p.descripcion, " +
             "p.precio, " +
             "p.stock, " +
             "p.estado, " +
             "p.id_categoria_fk, " +
             // ruta principal de la foto desde la tabla imagenes
             "i.url_ruta, " +
             // nombre de la categoria
             "c.nombre AS nombre_categoria, " +
             // aplicamos la misma subconsulta para el panel del proveedor
             "(SELECT GROUP_CONCAT(e.nombre_etiqueta SEPARATOR ',') FROM producto_etiqueta pe INNER JOIN etiqueta e ON pe.id_etiqueta = e.id_etiqueta_pk WHERE pe.id_producto = p.id_producto_pk) AS etiquetas_str " +
             // arrancamos en producto
             "FROM producto p " +
             "LEFT JOIN imagenes i ON p.id_producto_pk = i.id_producto_fk AND i.imagen_principal = 1 " +
             "LEFT JOIN categoria c ON p.id_categoria_fk = c.id_categoria_pk " +
             // USO DE INNER JOIN (cruce estricto): ¡aqui esta la magia de tu sistema!
             // inner join exige que el producto exista obligatoriamente en la tabla 'proveedor_producto'.
             // como los productos oficiales de dmari no estan en esa tabla puente, el inner join los descarta y los oculta.
             // asi garantizamos que el proveedor jamas vea un producto que no le pertenezca.
             "INNER JOIN proveedor_producto pp ON p.id_producto_pk = pp.id_producto_fk " +
             "INNER JOIN proveedor pr ON pp.id_proveedor_fk = pr.id_proveedor_pk " +
             "WHERE pr.id_proveedor_pk = ?";
             
        try (Connection con = db.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
             
             // inyectamos el id del usuario que esta en sesion actualmente
             ps.setInt(1, idUsuarioProveedor);
             
             try (ResultSet rs = ps.executeQuery()) {
                 // iteracion: convierte las filas recuperadas en el dto respectivo.
                 while(rs.next()){
                     producto prod = new producto();
                     prod.setIdProductoPk(rs.getInt("id_producto_pk"));
                     prod.setNombreProducto(rs.getString("nombre_producto"));
                     prod.setDescripcion(rs.getString("descripcion"));
                     prod.setPrecio(rs.getDouble("precio"));
                     prod.setStock(rs.getInt("stock"));
                     prod.setEstado(rs.getBoolean("estado"));
                     String rutaP = rs.getString("url_ruta");
                     prod.setUrlRuta(rutaP != null ? rutaP : "src/img/productos/default/gato_programador.jpg");
                     
                     prod.setIdCategoriaFk(rs.getInt("id_categoria_fk"));
                     String catP = rs.getString("nombre_categoria");
                     prod.setCategoria(catP != null ? catP : "General");
                     
                     String etiquetasStr = rs.getString("etiquetas_str");
                     ArrayList<String> listaTags = new ArrayList<>();
                     // condicional: fragmenta las etiquetas solo si existen.
                     if (etiquetasStr != null && !etiquetasStr.isEmpty()) {
                         String[] tagsArray = etiquetasStr.split(",");
                         for (String t : tagsArray) {
                             listaTags.add(t);
                         }
                     }
                     prod.setEtiquetas(listaTags);
                     
                     lista.add(prod);
                 }
             }
        } catch(SQLException error) {
            System.out.println("Problema en el dao al listar productos de proveedor: " + error.getMessage());
        }
        
        return lista;
    }

    /**
     * escritura base de datos: insertar producto maestro.
     * pide la creacion de la llave primaria para luego usarla en la insercion de fotos y etiquetas.
     * 
     * @param nuevoProducto producto: objeto lleno desde el servlet.
     * @return int: id que mysql genero automaticamente (mayor a 0 si exito).
     */
    public int insertarProducto(producto nuevoProducto){
        String sql = "insert into producto (nombre_producto, descripcion, precio, stock, id_categoria_fk, estado) values (?, ?, ?, ?, ?, ?)";
        int idGenerado = 0;

        try (Connection con = db.conectar();
            PreparedStatement ps = con.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, nuevoProducto.getNombreProducto());
            ps.setString(2, nuevoProducto.getDescripcion());
            ps.setDouble(3, nuevoProducto.getPrecio());
            ps.setInt(4, nuevoProducto.getStock());
            ps.setInt(5, nuevoProducto.getIdCategoriaFk());
            ps.setBoolean(6, nuevoProducto.isEstado());

            // condicional evaluativo: revisa si el insert arrojo confirmacion de insercion
            if (ps.executeUpdate() > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                // iteracion condicional: extrae la id
                if (rs.next()) {
                    idGenerado = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al insertar el producto: " + e.getMessage());
        }

        return idGenerado;
    }

    /**
     * puente vinculante entre vendedor y producto.
     * garantiza que el inventario jamas quede huerfano si lo creo una cuenta comercial.
     * 
     * @param idProducto int: articulo recien creado.
     * @param idUsuarioProveedor int: cuenta creadora.
     * @return boolean: true si el puente 'proveedor_producto' se forjo con exito.
     */
    public boolean asignarProductoAProveedor(int idProducto, int idUsuarioProveedor) {
        try (Connection con = db.conectar()) {
            int idProveedorPk = 0;
            
            // paso 1: ubicamos el id secundario de la tabla 'proveedor'
            String sqlBuscar = "SELECT id_proveedor_pk FROM proveedor WHERE id_proveedor_pk = ?";
            try (PreparedStatement psBuscar = con.prepareStatement(sqlBuscar)) {
                psBuscar.setInt(1, idUsuarioProveedor);
                try (ResultSet rs = psBuscar.executeQuery()) {
                    // condicional: si lo encontro, guardamos la credencial.
                    if (rs.next()) idProveedorPk = rs.getInt("id_proveedor_pk");
                }
            }
            
            // paso 2: enlazamos la mercancia
            // condicional limitante: prohibe puentes si la credencial fallo.
            if (idProveedorPk > 0) {
                String sqlInsert = "INSERT INTO proveedor_producto (id_proveedor_fk, id_producto_fk) VALUES (?, ?)";
                try (PreparedStatement psInsert = con.prepareStatement(sqlInsert)) {
                    psInsert.setInt(1, idProveedorPk);
                    psInsert.setInt(2, idProducto);
                    int filas = psInsert.executeUpdate();
                    System.out.println("Enlace creado exitosamente: producto " + idProducto + " -> proveedor " + idProveedorPk);
                    return filas > 0;
                }
            }
        } catch (SQLException e) {
            System.out.println("Error critico al asignar el proveedor: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * editor maestro de pertenencia comercial.
     * (exclusivo de rol admin) permite destruir un puente comercial y forjar otro.
     * 
     * @param idProducto int: articulo de destino.
     * @param idUsuarioProveedor int: nueva cuenta duena (si se pasa 0, queda expropiado a dmari).
     * @return boolean: confirmacion de transaccion.
     */
    public boolean actualizarProveedorDeProducto(int idProducto, int idUsuarioProveedor) {
        String sqlDelete = "DELETE FROM proveedor_producto WHERE id_producto_fk = ?";
        
        try (Connection con = db.conectar()) {
            try (PreparedStatement psDel = con.prepareStatement(sqlDelete)) {
                psDel.setInt(1, idProducto);
                psDel.executeUpdate();
            }
            
            // condicional funcional: si el admin eligio 0 ("sin proveedor"), esta insercion no se ejecuta.
            if (idUsuarioProveedor > 0) {
                int idProveedorPk = 0;
                String sqlBuscar = "SELECT id_proveedor_pk FROM proveedor WHERE id_proveedor_pk = ?";
                try (PreparedStatement psBuscar = con.prepareStatement(sqlBuscar)) {
                    psBuscar.setInt(1, idUsuarioProveedor);
                    try (ResultSet rs = psBuscar.executeQuery()) {
                        if (rs.next()) idProveedorPk = rs.getInt("id_proveedor_pk");
                    }
                }
                
                if (idProveedorPk > 0) {
                    String sqlInsert = "INSERT INTO proveedor_producto (id_proveedor_fk, id_producto_fk) VALUES (?, ?)";
                    try (PreparedStatement psInsert = con.prepareStatement(sqlInsert)) {
                        psInsert.setInt(1, idProveedorPk);
                        psInsert.setInt(2, idProducto);
                        psInsert.executeUpdate();
                    }
                }
            }
            return true;
        } catch (SQLException e) { return false; }
    }

    /**
     * funcion general de sobreescritura de tabla producto.
     * 
     * @param prod producto: dto con las cajas de texto actualizadas.
     * @return boolean: true si sobreescribio con exito.
     */
    public boolean actualizarProducto(producto prod) {
    String sql = "update producto set nombre_producto = ?, descripcion = ?, precio = ?, stock = ?, id_categoria_fk = ?, estado = ? where id_producto_pk = ?";

    try (Connection con = db.conectar();
        PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setString(1, prod.getNombreProducto());
        ps.setString(2, prod.getDescripcion());
        ps.setDouble(3, prod.getPrecio());
        ps.setInt(4, prod.getStock());
        ps.setInt(5, prod.getIdCategoriaFk());
        ps.setBoolean(6, prod.isEstado());
        
        ps.setInt(7, prod.getIdProductoPk());

        int filasAfectadas = ps.executeUpdate();

        // condicional de actualizacion: dictamina si el comando fue productivo.
        if (filasAfectadas > 0) {
            return true;
        }

    } catch (SQLException e) {
        System.out.println("Hubo un error al intentar actualizar: " + e.getMessage());
    }
    
    return false;
    }
    
    /**
     * metodo de borrado en cascada manual (transaccional).
     * destruye paulatinamente todas las referencias filiales antes de destruir al padre.
     * 
     * @param id int: numero de producto a destruir.
     * @return boolean: confirmacion final.
     */
    public boolean eliminarProducto(int id) {
        String sqlEtiquetas = "DELETE FROM producto_etiqueta WHERE id_producto = ?";
        String sqlProveedor = "DELETE FROM proveedor_producto WHERE id_producto_fk = ?";
        String sqlImagenes = "DELETE FROM imagenes WHERE id_producto_fk = ?";
        String sqlProducto = "DELETE FROM producto WHERE id_producto_pk = ?";
        
        String sqlLimpiarEtiquetas = "DELETE FROM etiqueta WHERE id_etiqueta_pk NOT IN (SELECT DISTINCT id_etiqueta FROM producto_etiqueta)";
        
        Connection con = null;
        try {
            con = db.conectar();
            con.setAutoCommit(false);

            try (PreparedStatement psEtiq = con.prepareStatement(sqlEtiquetas)) {
                psEtiq.setInt(1, id);
                psEtiq.executeUpdate();
            }

            try (PreparedStatement psProv = con.prepareStatement(sqlProveedor)) {
                psProv.setInt(1, id);
                psProv.executeUpdate();
            }

            try (PreparedStatement psImg = con.prepareStatement(sqlImagenes)) {
                psImg.setInt(1, id);
                psImg.executeUpdate();
            }

            int filasAfectadas = 0;
            try (PreparedStatement psProd = con.prepareStatement(sqlProducto)) {
                psProd.setInt(1, id);
                filasAfectadas = psProd.executeUpdate();
            }
            
            try (PreparedStatement psLimpiar = con.prepareStatement(sqlLimpiarEtiquetas)) {
                psLimpiar.executeUpdate();
            }

            con.commit();
            return filasAfectadas > 0;
            
        } catch (SQLException e) {
            try { if (con != null) con.rollback(); } catch (SQLException ex) {}
            System.out.println("No se pudo borrar el producto.. quiza este amarrado a un pedido: " + e.getMessage());
            return false;
        } finally {
            try { if (con != null) { con.setAutoCommit(true); con.close(); } } catch (SQLException e) {}
        }
    }

    /**
     * interruptor de disponibilidad rapida.
     * 
     * @param id int: target a pausar o reactivar.
     * @param nuevoEstado boolean: true para habilitar ventas, false para esconderlo.
     * @return boolean: true si acerto la instruccion sql.
     */
    public boolean actualizarEstado(int id, boolean nuevoEstado) {
        String sql = "update producto set estado = ? where id_producto_pk = ?";
        
        try (Connection con = db.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
             
            ps.setBoolean(1, nuevoEstado);
            ps.setInt(2, id);
            
            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;
            
        } catch (SQLException e) {
            System.out.println("Error al cambiar el estado del producto: " + e.getMessage());
        }
        
        return false;
    }
}
