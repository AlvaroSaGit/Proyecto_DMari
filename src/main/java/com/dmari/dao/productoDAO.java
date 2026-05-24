package com.dmari.dao;

/*
    DAO ESTRUCTURA CRUD
*/
/*
    Conexion database
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
    
    /*
        metodo para traer los productos de la base de datos.
        recibe un booleano para filtrar solo los activos y con stock.
    */
    public ArrayList<producto> listarProductos(boolean soloActivos){
        /*se crea una nueva lista*/
        ArrayList<producto> lista = new ArrayList<>();
        /*
            la sentencia sql base que se ejecutara en el motor de mysql
        */
        String sql = "SELECT " +
             // extraemos las columnas vitales de la tabla producto
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
             // creamos una mini subconsulta que busca en la tabla puente todas las etiquetas,
             // cruza con el nombre de la etiqueta y junta los textos separados por coma (ej: dulce,regalo)
             "(SELECT GROUP_CONCAT(e.nombre_etiqueta SEPARATOR ',') FROM producto_etiqueta pe INNER JOIN etiqueta e ON pe.id_etiqueta = e.id_etiqueta_pk WHERE pe.id_producto = p.id_producto_pk) AS etiquetas_str " +
             // tabla principal desde donde partimos
             "FROM producto p " +
             // uso de left join: traemos el producto incluso si no tiene imagen asignada
             "LEFT JOIN imagenes i ON p.id_producto_pk = i.id_producto_fk AND i.imagen_principal = 1 " +
             // uso de left join: traemos el nombre de la categoria. si el producto quedo sin categoria por algun error, no se ocultara.
             "LEFT JOIN categoria c ON p.id_categoria_fk = c.id_categoria_pk";
        
        // si el parametro es verdadero, concatenamos la condicion a la consulta
        if (soloActivos) {
            sql += " WHERE p.estado = 1 AND p.stock > 0";
        }
        
        // AGRUPACION ESTRICTA: Fuerza a MySQL a aplastar filas duplicadas generadas por cruces multiples (JOINs)
        sql += " GROUP BY p.id_producto_pk";

        /*
            usar el try como try-with-resources,
            esto hace que se cierre automaticamente al llegar
            a la llave de cierre como metodo de seguridad
        */
        try(
            /*
                aqui se llama el metodo conectar() de la clase databasehelper.
                se abre el flujo de datos hacia mysql.
            */
            Connection con = db.conectar();
            /*
                se usa la conexion abierta para preparar el comando sql.
                preparedstatement es mas seguro porque evita ataques de
                inyeccion sql
            */
            PreparedStatement ps = con.prepareStatement(sql);
            /*
                manda la orden a mysql
            */
            ResultSet rs = ps.executeQuery()){
            
            /*
                mientras el resultset tenga filas por leer...
            */
            while(rs.next()){
                /*
                se crea un objeto producto que viene del archivo
                producto de com.dmari.modelo.
                */
                producto prod = new producto();
                /*
                    sacamos los datos de las columnas mysql
                    y se lo colocamos en los atributos del objeto.
                */
                prod.setIdProductoPk(rs.getInt("id_producto_pk"));
                prod.setNombreProducto(rs.getString("nombre_producto"));
                prod.setDescripcion(rs.getString("descripcion"));
                prod.setPrecio(rs.getDouble("precio"));
                prod.setStock(rs.getInt("stock"));
                prod.setEstado(rs.getBoolean("estado"));
                prod.setUrlRuta(rs.getString("url_ruta"));
                
                /* Extraemos el ID y el nombre de la categoria */
                prod.setIdCategoriaFk(rs.getInt("id_categoria_fk"));
                prod.setCategoria(rs.getString("nombre_categoria"));
                
                // extraemos la cadena de multiples etiquetas y la convertimos en un arreglo (lista)
                String etiquetasStr = rs.getString("etiquetas_str");
                ArrayList<String> listaTags = new ArrayList<>();
                if (etiquetasStr != null && !etiquetasStr.isEmpty()) {
                    String[] tagsArray = etiquetasStr.split(",");
                    for (String t : tagsArray) {
                        listaTags.add(t);
                    }
                }
                prod.setEtiquetas(listaTags);

                /*metemos el producto ya lleno en la lista general*/
                lista.add(prod);
            }
        }catch(SQLException error){
            System.out.println("problema en el dao al listar productos: "+error.getMessage());
        }
        
        return lista;
    }
    
    /*
        metodo para listar los productos que le pertenecen a un proveedor especifico.
        no filtra por activos o inactivos, ya que el proveedor debe ver todos sus productos.
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
             "WHERE pr.id_datos_proveedor_fk = ? " +
             "GROUP BY p.id_producto_pk";
             
        try (Connection con = db.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
             
             // inyectamos el id del usuario que esta en sesion actualmente
             ps.setInt(1, idUsuarioProveedor);
             
             try (ResultSet rs = ps.executeQuery()) {
                 while(rs.next()){
                     producto prod = new producto();
                     prod.setIdProductoPk(rs.getInt("id_producto_pk"));
                     prod.setNombreProducto(rs.getString("nombre_producto"));
                     prod.setDescripcion(rs.getString("descripcion"));
                     prod.setPrecio(rs.getDouble("precio"));
                     prod.setStock(rs.getInt("stock"));
                     prod.setEstado(rs.getBoolean("estado"));
                     prod.setUrlRuta(rs.getString("url_ruta"));
                     
                     prod.setIdCategoriaFk(rs.getInt("id_categoria_fk"));
                     prod.setCategoria(rs.getString("nombre_categoria"));
                     
                     String etiquetasStr = rs.getString("etiquetas_str");
                     ArrayList<String> listaTags = new ArrayList<>();
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
            System.out.println("problema en el dao al listar productos de proveedor: " + error.getMessage());
        }
        
        return lista;
    }

    public int insertarProducto(producto nuevoProducto){
        // aca solo dejamos la consulta del producto solo
        String sql = "insert into producto (nombre_producto, descripcion, precio, stock, id_categoria_fk, estado) values (?, ?, ?, ?, ?, ?)";
        int idGenerado = 0;

        try (Connection con = db.conectar();
                
            // el try con parentesis abre la conexion a la base de datos y la cierra al final para no saturar la memoria
            // el preparestatement prepara la orden sql de forma segura,
            // y el return_generated_keys le exige a mysql que nos devuelva el id automatico que le acaba de asignar a ese producto nuevo   
            PreparedStatement ps = con.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            // pasamos los datos basicos del producto
            ps.setString(1, nuevoProducto.getNombreProducto());
            ps.setString(2, nuevoProducto.getDescripcion());
            ps.setDouble(3, nuevoProducto.getPrecio());
            ps.setInt(4, nuevoProducto.getStock());
            ps.setInt(5, nuevoProducto.getIdCategoriaFk());
            ps.setBoolean(6, nuevoProducto.isEstado());

            // ejecutamos el insert
            if (ps.executeUpdate() > 0) {
                // getgeneratedkeys sirve para que mysql nos diga que id le puso a la dona o vela
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    // guardamos ese numero en nuestra variable
                    idGenerado = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.out.println("error al insertar el producto: " + e.getMessage());
        }

        // devolvemos el id.. si es 0 significa que no se guardo nada
        return idGenerado;

    }

    // metodo para enlazar un producto nuevo con el proveedor que lo acaba de crear en el sistema.
    // este metodo es vital porque los productos no pueden quedar huerfanos si los crea un proveedor.
    public boolean asignarProductoAProveedor(int idProducto, int idUsuarioProveedor) {
        try (Connection con = db.conectar()) {
            int idProveedorPk = 0;
            
            // paso 1: buscamos cual es el id interno del proveedor en su tabla principal.
            // esto se hace porque la tabla puente requiere el id de 'proveedor', no el id general de 'usuario'.
            String sqlBuscar = "SELECT id_proveedor_pk FROM proveedor WHERE id_datos_proveedor_fk = ?";
            try (PreparedStatement psBuscar = con.prepareStatement(sqlBuscar)) {
                psBuscar.setInt(1, idUsuarioProveedor);
                try (ResultSet rs = psBuscar.executeQuery()) {
                    if (rs.next()) idProveedorPk = rs.getInt("id_proveedor_pk");
                }
            }
            
            // paso 2: finalmente, hacemos el enlace directo y exacto a la tabla puente.
            // unimos el id del producto con el id interno del proveedor validado.
            if (idProveedorPk > 0) {
                String sqlInsert = "INSERT INTO proveedor_producto (id_proveedor_fk, id_producto_fk) VALUES (?, ?)";
                try (PreparedStatement psInsert = con.prepareStatement(sqlInsert)) {
                    psInsert.setInt(1, idProveedorPk);
                    psInsert.setInt(2, idProducto);
                    int filas = psInsert.executeUpdate();
                    System.out.println("enlace creado exitosamente: producto " + idProducto + " -> proveedor " + idProveedorPk);
                    return filas > 0;
                }
            }
        } catch (SQLException e) {
            System.out.println("error critico al asignar el proveedor: " + e.getMessage());
        }
        return false;
    }
    
    // metodo para actualizar o quitar al proveedor de un producto existente (exclusivo del administrador).
    public boolean actualizarProveedorDeProducto(int idProducto, int idUsuarioProveedor) {
        // preparamos la orden para destruir cualquier conexion vieja.
        String sqlDelete = "DELETE FROM proveedor_producto WHERE id_producto_fk = ?";
        
        try (Connection con = db.conectar()) {
            // primero borramos cualquier conexion previa para que no queden duplicados (huerfanos) en la tabla puente.
            try (PreparedStatement psDel = con.prepareStatement(sqlDelete)) {
                psDel.setInt(1, idProducto);
                psDel.executeUpdate();
            }
            
            // si el admin selecciono a un proveedor real del menu (id > 0), creamos el nuevo enlace.
            // si selecciono "sin proveedor", el id llega como 0, se salta este bloque y el producto queda libre.
            if (idUsuarioProveedor > 0) {
                int idProveedorPk = 0;
                // buscamos su id interno en la tabla
                String sqlBuscar = "SELECT id_proveedor_pk FROM proveedor WHERE id_datos_proveedor_fk = ?";
                try (PreparedStatement psBuscar = con.prepareStatement(sqlBuscar)) {
                    psBuscar.setInt(1, idUsuarioProveedor);
                    try (ResultSet rs = psBuscar.executeQuery()) {
                        if (rs.next()) idProveedorPk = rs.getInt("id_proveedor_pk");
                    }
                }
                
                // creamos el nuevo puente en la base de datos
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

    public boolean actualizarProducto(producto prod) {
    // actualiza la tabla principal del producto
    // el where es vital: usamos id_producto_pk para asegurar que solo cambiamos ese item especifico
    String sql = "update producto set nombre_producto = ?, descripcion = ?, precio = ?, stock = ?, id_categoria_fk = ?, estado = ? where id_producto_pk = ?";

    // abrimos conexion y preparamos la consulta de una vez
    try (Connection con = db.conectar();
        PreparedStatement ps = con.prepareStatement(sql)) {

        // reemplazamos los signos de interrogacion con los datos nuevos
        ps.setString(1, prod.getNombreProducto());
        ps.setString(2, prod.getDescripcion());
        ps.setDouble(3, prod.getPrecio());
        ps.setInt(4, prod.getStock());
        ps.setInt(5, prod.getIdCategoriaFk());
        ps.setBoolean(6, prod.isEstado());
        
        // el quinto parametro es el id, para decirle a mysql cual producto exacto debe cambiar
        ps.setInt(7, prod.getIdProductoPk());

        // ejecutamos la orden y guardamos cuantas filas se modificaron
        int filasAfectadas = ps.executeUpdate();

        // si el numero es mayor a 0, significa que si encontro el producto y lo actualizo
        if (filasAfectadas > 0) {
            return true;
        }

    } catch (SQLException e) {
        System.out.println("hubo un error al intentar actualizar: " + e.getMessage());
    }
    
    // si no lo encontro o hubo error, devuelve falso
    return false;
    }
    
    // metodo para borrar un producto de la tienda
    public boolean eliminarProducto(int id) {
        // preparamos las sentencias para borrar primero los registros que dependen de este producto
        // 1. borramos las relaciones en la tabla puente de etiquetas
        String sqlEtiquetas = "DELETE FROM producto_etiqueta WHERE id_producto = ?";
        // 2. borramos la pertenencia en la tabla del proveedor
        String sqlProveedor = "DELETE FROM proveedor_producto WHERE id_producto_fk = ?";
        // 3. borramos todas las rutas fotograficas de este producto
        String sqlImagenes = "DELETE FROM imagenes WHERE id_producto_fk = ?";
        // 4. despues de vaciar a los hijos, finalmente borramos al padre (el producto)
        String sqlProducto = "DELETE FROM producto WHERE id_producto_pk = ?";
        
        // 5. recolector de basura: eliminamos etiquetas huerfanas tras el borrado
        String sqlLimpiarEtiquetas = "DELETE FROM etiqueta WHERE id_etiqueta_pk NOT IN (SELECT DISTINCT id_etiqueta FROM producto_etiqueta)";
        
        Connection con = null;
        try {
            con = db.conectar();
            // apagamos el autocommit para hacer un borrado en cascada manual y seguro
            con.setAutoCommit(false);

            // 1. desvincular etiquetas
            try (PreparedStatement psEtiq = con.prepareStatement(sqlEtiquetas)) {
                psEtiq.setInt(1, id);
                psEtiq.executeUpdate();
            }

            // 2. desvincular de los proveedores
            try (PreparedStatement psProv = con.prepareStatement(sqlProveedor)) {
                psProv.setInt(1, id);
                psProv.executeUpdate();
            }

            // 3. borrar imagenes asociadas
            try (PreparedStatement psImg = con.prepareStatement(sqlImagenes)) {
                psImg.setInt(1, id);
                psImg.executeUpdate();
            }

            // 4. finalmente, borrar el producto maestro
            int filasAfectadas = 0;
            try (PreparedStatement psProd = con.prepareStatement(sqlProducto)) {
                psProd.setInt(1, id);
                filasAfectadas = psProd.executeUpdate();
            }
            
            // 5. limpiar etiquetas huerfanas generadas por esta eliminacion
            try (PreparedStatement psLimpiar = con.prepareStatement(sqlLimpiarEtiquetas)) {
                psLimpiar.executeUpdate();
            }

            // si todo salio bien, confirmamos los cambios en mysql
            con.commit();
            return filasAfectadas > 0;
            
        } catch (SQLException e) {
            // si explota (por ejemplo, porque el producto ya esta en un pedido), revertimos todo
            try { if (con != null) con.rollback(); } catch (SQLException ex) {}
            System.out.println("no se pudo borrar el producto.. quiza este amarrado a un pedido: " + e.getMessage());
            return false;
        } finally {
            // restauramos el comportamiento normal de la conexion
            try { if (con != null) { con.setAutoCommit(true); con.close(); } } catch (SQLException e) {}
        }
    }

    // metodo especifico para cambiar solo el estado de un producto (activo/inactivo)
    public boolean actualizarEstado(int id, boolean nuevoEstado) {
        // instruccion que afecta solo la columna 'estado' del articulo filtrado por su id
        String sql = "update producto set estado = ? where id_producto_pk = ?";
        
        try (Connection con = db.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
             
            ps.setBoolean(1, nuevoEstado);
            ps.setInt(2, id);
            
            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;
            
        } catch (SQLException e) {
            System.out.println("error al cambiar el estado del producto: " + e.getMessage());
        }
        
        return false;
    }
}
