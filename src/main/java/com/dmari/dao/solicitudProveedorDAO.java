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
     * @param idUsuario int: el id del usuario que realiza la solicitud.
     * @param nit string: el nit de la empresa.
     * @param marca string: el nombre de la marca.
     * @param cuenta string: el numero de cuenta bancaria.
     * @param banco string: el nombre del banco.
     * @param tipoCuenta string: el tipo de cuenta (ahorros/corriente).
     * @return boolean: true si la solicitud se creo con exito.
     */
    public boolean crearSolicitud(int idUsuario, String nit, String marca, String cuenta, String banco, String tipoCuenta) {
        // sql para insertar la nueva peticion del usuario en la tabla solicitud_proveedor
        // el estado_solicitud inicial siempre sera 'pendiente'
        String sql = "INSERT INTO solicitud_proveedor (id_usuario_fk, nit_empresa, nombre_marca, cuenta_bancaria, banco_nombre, tipo_cuenta, estado_solicitud) VALUES (?, ?, ?, ?, ?, ?, 'pendiente')";
        
        // abrimos la conexion y preparamos la consulta al mismo tiempo (try-with-resources)
        // esto asegura que la conexion se cierre automaticamente al final
        try (Connection con = db.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
             
            // asignamos cada parametro que recibio el metodo a los signos de interrogacion (?) en orden
            ps.setInt(1, idUsuario);
            ps.setString(2, nit);
            ps.setString(3, marca);
            ps.setString(4, cuenta);
            ps.setString(5, banco);
            ps.setString(6, tipoCuenta);
            
            // ejecutamos la insercion en la base de datos
            // executeupdate devuelve el numero de filas afectadas. si es mayor a 0, guardo exitosamente
            return ps.executeUpdate() > 0;
            
        } catch (SQLException e) {
            // si ocurre algun error (por ejemplo, base de datos caida), lo mostramos en consola
            System.err.println("error al crear solicitud de proveedor: " + e.getMessage());
            return false;
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
        // unimos (inner join) la tabla de solicitudes con la tabla usuario para ver el nombre real de quien la envia.
        // usamos left join con correo por si queremos extraer el email, aunque no lo usemos todo.
        String sql = "SELECT sp.*, u.nombre, c.correo FROM solicitud_proveedor sp " +
                     "INNER JOIN usuario u ON sp.id_usuario_fk = u.id_usuario_pk " +
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
     * accion compleja: acepta al proveedor, le da acceso al sistema y guarda su info financiera.
     * requiere transaccion (commit/rollback) porque altera 3 tablas distintas.
     * @param idSolicitud int: el id de la solicitud en cuestion.
     * @return boolean: true si todo el proceso fue exitoso.
     */
    public boolean aprobarSolicitud(int idSolicitud) {
        // sql 1: primero necesitamos leer los datos originales de la solicitud para copiarlos al perfil final
        String sqlSelect = "SELECT * FROM solicitud_proveedor WHERE id_solicitud_pk = ?";
        // sql 2: actualizamos el estado en la tabla de solicitudes a 'aprobada'
        String sqlUpdateSol = "UPDATE solicitud_proveedor SET estado_solicitud = 'aprobada' WHERE id_solicitud_pk = ?";
        // sql 3: le devolvemos el acceso al usuario quitando su estado bloqueado o pendiente (1 = activo)
        String sqlUpdateUsr = "UPDATE usuario SET estado_cuenta = 1 WHERE id_usuario_pk = ?";
        // sql 4: copiamos su nit y cuentas bancarias a la tabla definitiva de proveedores para que pueda cobrar
        String sqlInsertProv = "INSERT INTO proveedor (id_proveedor_pk, nit_empresa, nombre_marca, cuenta_bancaria, banco_nombre, tipo_cuenta) VALUES (?, ?, ?, ?, ?, ?)";
        
        Connection con = db.conectar();
        if(con == null) return false;
        
        try {
            // apagamos el autoguardado porque haremos multiples cambios. si uno falla, nada se guarda.
            con.setAutoCommit(false);
            
            // variables para sostener en memoria la informacion leida de la solicitud
            int idUsuario = 0;
            String nit = "", marca = "", cuenta = "", banco = "", tipo = "";
            
            // paso 1: consultar la solicitud
            try (PreparedStatement psSel = con.prepareStatement(sqlSelect)) {
                psSel.setInt(1, idSolicitud);
                java.sql.ResultSet rs = psSel.executeQuery();
                // si encontramos la solicitud, extraemos sus datos a la memoria
                if(rs.next()) {
                    idUsuario = rs.getInt("id_usuario_fk");
                    nit = rs.getString("nit_empresa");
                    marca = rs.getString("nombre_marca");
                    cuenta = rs.getString("cuenta_bancaria");
                    banco = rs.getString("banco_nombre");
                    tipo = rs.getString("tipo_cuenta");
                } else {
                    // si por alguna razon no existe, abortamos
                    return false;
                }
            }
            
            // paso 2: marcar la solicitud como aprobada
            try (PreparedStatement psSol = con.prepareStatement(sqlUpdateSol)) {
                psSol.setInt(1, idSolicitud);
                psSol.executeUpdate();
            }
            
            // paso 3: habilitar el inicio de sesion del usuario (estado_cuenta = 1)
            try (PreparedStatement psUsr = con.prepareStatement(sqlUpdateUsr)) {
                psUsr.setInt(1, idUsuario);
                psUsr.executeUpdate();
            }
            
            // paso 4: insertar su perfil de cobro en la tabla oficial de proveedores
            try (PreparedStatement psProv = con.prepareStatement(sqlInsertProv)) {
                psProv.setInt(1, idUsuario); // el id_proveedor_pk es exactamente el mismo id de usuario
                psProv.setString(2, nit);
                psProv.setString(3, marca);
                psProv.setString(4, cuenta);
                psProv.setString(5, banco);
                psProv.setString(6, tipo);
                psProv.executeUpdate();
            }
            
            // si todo salio bien, confirmamos los 3 cambios en bloque
            con.commit();
            return true;
            
        } catch (SQLException e) {
            // si algo explota, deshacemos todo lo que hicimos en esta peticion
            try { con.rollback(); } catch(SQLException ex) {}
            System.err.println("error aprobar solicitud proveedor: " + e.getMessage());
            return false;
        } finally {
            // encendemos el autoguardado para no afectar otras consultas
            try { con.setAutoCommit(true); con.close(); } catch(SQLException ex) {}
        }
    }

    /**
     * 4. rechazar solicitud
     * simplemente actualiza la solicitud a estado rechazado. no habilita al usuario ni inserta nada.
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