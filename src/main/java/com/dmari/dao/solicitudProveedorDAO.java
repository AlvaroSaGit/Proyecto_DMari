package com.dmari.dao;

import com.dmari.helper.databaseHelper;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class solicitudProveedorDAO {
    // instanciamos la clase de ayuda que nos da la conexion a la base de datos
    private databaseHelper db = new databaseHelper();

    /**
     * 1. crear solicitud
     * crea una nueva solicitud para que un usuario se convierta en proveedor.
     * ahora, con el nuevo esquema, inserta primero en la tabla proveedor y luego en solicitud_proveedor.
     * ademas suspende la cuenta del usuario para revision.
     * @param idUsuario int: el id del usuario que realiza la solicitud.
     * @param nit string: el nit de la empresa.
     * @param marca string: el nombre de la marca.
     * @param cuenta string: el numero de cuenta bancaria.
     * @param banco string: el nombre del banco.
     * @param tipoCuenta string: el tipo de cuenta (ahorros/corriente).
     * @return boolean: true si la solicitud se creo con exito.
     */
    public boolean crearSolicitud(int idUsuario, String nit, String marca, String cuenta, String banco, String tipoCuenta) {
        // sql 1: insertamos los datos comerciales en la tabla definitiva de proveedor
        String sqlProv = "INSERT INTO proveedor (id_proveedor_pk, nit_empresa, nombre_marca, cuenta_bancaria, banco_nombre, tipo_cuenta) VALUES (?, ?, ?, ?, ?, ?)";
        // sql 2: insertamos la solicitud apuntando al proveedor recien creado
        String sqlSol = "INSERT INTO solicitud_proveedor (id_proveedor_fk, estado_solicitud) VALUES (?, 'pendiente')";
        // sql 3: pausamos la cuenta del usuario para que el admin la apruebe
        String sqlPausar = "UPDATE usuario SET estado_cuenta = 0 WHERE id_usuario_pk = ?";
        
        // Formatear tipoCuenta para que coincida con el ENUM de MySQL ('Ahorros', 'Corriente')
        if (tipoCuenta != null && !tipoCuenta.isEmpty()) {
            tipoCuenta = tipoCuenta.substring(0, 1).toUpperCase() + tipoCuenta.substring(1).toLowerCase();
        }
        
        Connection con = null;
        try {
            // abrimos la conexion a la base de datos
            con = db.conectar();
            // desactivamos el autocommit para tratar todo como una sola transaccion
            con.setAutoCommit(false);
            
            // paso 1: insertamos el perfil en la tabla proveedor
            try (PreparedStatement psProv = con.prepareStatement(sqlProv)) {
                // asignamos el id del usuario que ahora sera la llave primaria del proveedor
                psProv.setInt(1, idUsuario);
                // asignamos el nit de la empresa
                psProv.setString(2, nit);
                // asignamos el nombre de la marca
                psProv.setString(3, marca);
                // asignamos el numero de cuenta bancaria
                psProv.setString(4, cuenta);
                // asignamos el nombre del banco
                psProv.setString(5, banco);
                // asignamos el tipo de cuenta (ahorros o corriente)
                psProv.setString(6, tipoCuenta);
                // ejecutamos el query de insercion del proveedor
                psProv.executeUpdate();
            }
            
            // paso 2: insertamos el registro en la tabla de solicitudes
            try (PreparedStatement psSol = con.prepareStatement(sqlSol)) {
                // el id del proveedor fk es el mismo id del usuario
                psSol.setInt(1, idUsuario);
                // ejecutamos el query de insercion de la solicitud
                psSol.executeUpdate();
            }

            // paso 3: pausamos el acceso del usuario
            try (PreparedStatement psPause = con.prepareStatement(sqlPausar)) {
                // asignamos el id del usuario a pausar
                psPause.setInt(1, idUsuario);
                // ejecutamos el update en la tabla usuario
                psPause.executeUpdate();
            }
            
            // si llegamos aqui sin errores, confirmamos todos los cambios en bloque
            con.commit();
            // retornamos verdadero indicando que todo salio bien
            return true;
            
        } catch (SQLException e) {
            // si hay algun error, hacemos rollback para deshacer cualquier cambio parcial
            try { if (con != null) con.rollback(); } catch(SQLException ex) {}
            // imprimimos el error en consola para depurar
            System.err.println("error al crear solicitud de proveedor: " + e.getMessage());
            // devolvemos falso indicando fallo
            return false;
        } finally {
            // siempre volvemos a activar el autocommit y cerramos la conexion
            try { if (con != null) { con.setAutoCommit(true); con.close(); } } catch(SQLException ex) {}
        }
    }

    /**
     * 2. listar solicitudes
     * obtiene todas las solicitudes de los usuarios para mostrarlas en el panel de admin.
     * @return list<map<string, string>>: una lista de diccionarios (mapas) con los datos de las solicitudes.
     */
    public java.util.List<java.util.Map<String, String>> listarSolicitudes() {
        // creamos una lista vacia para guardar todas las peticiones encontradas
        java.util.List<java.util.Map<String, String>> lista = new java.util.ArrayList<>();
        
        // sql para consultar la informacion.
        // unimos (inner join) la tabla de solicitudes con la tabla proveedor para obtener los datos comerciales.
        // unimos (inner join) la tabla de proveedor con usuario para obtener el nombre del solicitante.
        // usamos left join con correo por si queremos extraer el email.
        String sql = "SELECT sp.id_solicitud_pk, u.nombre, c.correo, p.nit_empresa, p.nombre_marca, " +
                     "p.cuenta_bancaria, p.banco_nombre, p.tipo_cuenta, sp.estado_solicitud, p.id_proveedor_pk as id_usuario_fk " +
                     "FROM solicitud_proveedor sp " +
                     "INNER JOIN proveedor p ON sp.id_proveedor_fk = p.id_proveedor_pk " +
                     "INNER JOIN usuario u ON p.id_proveedor_pk = u.id_usuario_pk " +
                     "LEFT JOIN correo c ON u.id_usuario_pk = c.id_usuario_fk " +
                     "ORDER BY sp.id_solicitud_pk DESC";
                     
        // conectamos y preparamos la consulta y tambien ejecutamos la lectura (resultset)
        try (Connection con = db.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             java.sql.ResultSet rs = ps.executeQuery()) {
             
            // iteracion: leemos fila por fila los resultados que nos trajo mysql
            while (rs.next()) {
                // creamos un mapa (diccionario clave-valor) para guardar la informacion de esta fila
                java.util.Map<String, String> map = new java.util.HashMap<>();
                
                // leemos las columnas de la fila y las guardamos en el mapa convirtiendolas a texto (string)
                map.put("id", String.valueOf(rs.getInt("id_solicitud_pk")));
                map.put("usuarioNombre", rs.getString("nombre"));
                map.put("usuarioCorreo", rs.getString("correo"));
                map.put("nit", rs.getString("nit_empresa"));
                map.put("marca", rs.getString("nombre_marca"));
                map.put("cuenta", rs.getString("cuenta_bancaria"));
                map.put("banco", rs.getString("banco_nombre"));
                map.put("tipoCuenta", rs.getString("tipo_cuenta"));
                map.put("estado", rs.getString("estado_solicitud"));
                map.put("idUsuarioFk", String.valueOf(rs.getInt("id_usuario_fk")));
                
                // anadimos el diccionario recien armado a nuestra lista principal
                lista.add(map);
            }
        } catch (SQLException e) {
            // capturamos errores de lectura
            System.err.println("error listar solicitudes proveedor: " + e.getMessage());
        }
        return lista; // devolvemos la lista llena o vacia si no hay nada
    }

    /**
     * 3. aprobar solicitud
     * accion que acepta al proveedor: como los datos ya estan en la tabla proveedor,
     * solo actualiza el estado de la solicitud y activa la cuenta del usuario otorgandole rol 4.
     * requiere transaccion (commit/rollback) porque altera 2 tablas distintas.
     * @param idSolicitud int: el id de la solicitud en cuestion.
     * @return boolean: true si todo el proceso fue exitoso.
     */
    public boolean aprobarSolicitud(int idSolicitud) {
        // sql 1: primero necesitamos el id del proveedor desde la solicitud
        String sqlSelect = "SELECT id_proveedor_fk FROM solicitud_proveedor WHERE id_solicitud_pk = ?";
        // sql 2: actualizamos el estado en la tabla de solicitudes a 'aprobada'
        String sqlUpdateSol = "UPDATE solicitud_proveedor SET estado_solicitud = 'aprobada' WHERE id_solicitud_pk = ?";
        // sql 3: activamos la cuenta y cambiamos el rol del usuario a proveedor (4)
        String sqlUpdateUsr = "UPDATE usuario SET estado_cuenta = 1, id_rol_fk = 4 WHERE id_usuario_pk = ?";
        
        Connection con = db.conectar();
        if(con == null) return false;
        
        try {
            // apagamos el autoguardado porque haremos multiples cambios. si uno falla, nada se guarda.
            con.setAutoCommit(false);
            
            // variable para guardar el id del proveedor que vamos a aprobar
            int idProveedor = 0;
            
            // paso 1: consultar la solicitud para obtener el id_proveedor_fk
            try (PreparedStatement psSel = con.prepareStatement(sqlSelect)) {
                // enviamos el parametro de la solicitud a buscar
                psSel.setInt(1, idSolicitud);
                // ejecutamos la busqueda
                java.sql.ResultSet rs = psSel.executeQuery();
                // si la solicitud existe
                if(rs.next()) {
                    // obtenemos el id_proveedor_fk (que es el mismo id_usuario)
                    idProveedor = rs.getInt("id_proveedor_fk");
                } else {
                    // si por alguna razon no existe, abortamos
                    return false;
                }
            }
            
            // paso 2: marcar la solicitud como aprobada
            try (PreparedStatement psSol = con.prepareStatement(sqlUpdateSol)) {
                // asignamos el id de la solicitud a actualizar
                psSol.setInt(1, idSolicitud);
                // ejecutamos el update
                psSol.executeUpdate();
            }
            
            // paso 3: habilitar el inicio de sesion del usuario y cambiar su rol a 4
            try (PreparedStatement psUsr = con.prepareStatement(sqlUpdateUsr)) {
                // asignamos el id del usuario que ahora sera formalmente proveedor
                psUsr.setInt(1, idProveedor);
                // ejecutamos el update en la tabla usuario
                psUsr.executeUpdate();
            }
            
            // si todo salio bien, confirmamos los 2 cambios en bloque
            con.commit();
            // devolvemos true por exito
            return true;
            
        } catch (SQLException e) {
            // si algo explota, deshacemos todo lo que hicimos en esta peticion
            try { con.rollback(); } catch(SQLException ex) {}
            // reportamos en consola
            System.err.println("error aprobar solicitud proveedor: " + e.getMessage());
            // devolvemos falso
            return false;
        } finally {
            // encendemos el autoguardado para no afectar otras consultas
            try { con.setAutoCommit(true); con.close(); } catch(SQLException ex) {}
        }
    }

    /**
     * 4. rechazar solicitud
     * simplemente actualiza la solicitud a estado rechazado.
     * @param idSolicitud int: el id de la peticion.
     * @return boolean: true si el cambio se guardo.
     */
    public boolean rechazarSolicitud(int idSolicitud) {
        // sql para cambiar la columna de estado
        String sql = "UPDATE solicitud_proveedor SET estado_solicitud = 'rechazada' WHERE id_solicitud_pk = ?";
        try (Connection con = db.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
             
            // asignamos el parametro
            ps.setInt(1, idSolicitud);
            // ejecutamos y comprobamos que altero al menos 1 fila
            return ps.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("error rechazar solicitud proveedor: " + e.getMessage());
            return false;
        }
    }
}